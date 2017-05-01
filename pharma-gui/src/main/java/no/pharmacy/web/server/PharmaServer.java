package no.pharmacy.web.server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumSet;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import lombok.Getter;
import lombok.Setter;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.infrastructure.logging.LogConfiguration;
import no.pharmacy.medication.FestMedicationImporter;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.organization.JdbcHealthcareServiceRepository;
import no.pharmacy.patient.JdbcPatientRepository;
import no.pharmacy.practitioner.JdbcPractitionerRepository;
import no.pharmacy.practitioner.PractitionerRepository;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.PrescriptionSimulator;
import no.pharmacy.web.infrastructure.auth.AuthenticationConfiguration;
import no.pharmacy.web.infrastructure.auth.AuthenticationFilter;
import no.pharmacy.web.infrastructure.auth.IdCheckServlet;
import no.pharmacy.web.infrastructure.logging.LogDisplayServlet;
import no.pharmacy.web.test.PharmaTestCaseApiController;
import no.pharmacy.web.test.ReceiptTestCaseController;
import no.pharmacy.web.test.ReseptFormidlerLogTestController;

public class PharmaServer {

    private static final String SHUTDOWN_TOKEN = "c5d4a90d-e4be-414c-ad8a-2a172c31a230";

    private static final Logger logger = LoggerFactory.getLogger(PharmaServer.class);

    private LogConfiguration logConfiguration = new LogConfiguration();
    private Server server;

    private LogDisplayServlet logServlet;

    private AuthenticationConfiguration authConfig;

    @Getter
    private PharmaApplicationContext applicationContext;

    @Setter
    private PrescriptionSimulator prescriptionSimulator;

    public PharmaServer(int port, AuthenticationConfiguration authConfig, PharmaApplicationContext applicationContext) {
        logConfiguration.setLevel("org.eclipse.jetty", Level.WARN);
        logConfiguration.setLevel("org.flywaydb", Level.WARN);
        logServlet = new LogDisplayServlet(logConfiguration.getContext());

        this.authConfig = authConfig;

        server = new Server(port);
        server.addLifeCycleListener(Server.STOP_ON_FAILURE);
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (System.getenv("HTTP_PORT") != null) {
            port = Integer.parseInt(System.getenv("HTTP_PORT"));
        }

        JdbcMedicationRepository medicationRepository = new JdbcMedicationRepository(DataSources.createMedicationDataSource());
        medicationRepository.refresh(FestMedicationImporter.FEST_URL);

        PharmaServer pharmaServer = new PharmaServer(port, new AuthenticationConfiguration(), new PharmaApplicationContext(medicationRepository));
        pharmaServer.getApplicationContext().setHealthcareServiceRepository(createHealthcareRepository());
        pharmaServer.getApplicationContext().setPractitionerRepository(createPractitionerRepository());
        pharmaServer.getApplicationContext().setPatientRepository(createPatientRepository());
        pharmaServer.getApplicationContext().setPharmacistDataSource(DataSources.createPharmaDataSource());

        FakeReseptFormidler reseptFormidler = new FakeReseptFormidler(medicationRepository, createPatientRepository());
        pharmaServer.getApplicationContext().setRfMessageGateway(reseptFormidler);
        pharmaServer.setPrescriptionSimulator(reseptFormidler);
        pharmaServer.start();
    }

    private static JdbcPatientRepository createPatientRepository() {
        return new JdbcPatientRepository(DataSources.createPatientDataSource(),
                s -> new PharmaTestData().samplePatient(),
                CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));
    }

    public void start() throws Exception {
        server.setHandler(createHandlers());
        server.start();
        logger.warn("Started http://localhost:{}", server.getURI().getPort());
    }

    private HandlerList createHandlers() throws IOException, URISyntaxException {
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ShutdownHandler(SHUTDOWN_TOKEN, true, true));

        PharmaGuiHandler guiHandler = new PharmaGuiHandler(authConfig, applicationContext);

        handlers.addHandler(createOpsHandler());
        handlers.addHandler(createPharmaTestRig());
        handlers.addHandler(guiHandler.createHandler());

        return handlers;
    }

    private Handler createPharmaTestRig() {
        WebAppContext handler = new WebAppContext(null, "/pharma-test");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-testrig-webapp"));
        // Avoid locking files on disk
        handler.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");

        handler.addServlet(new ServletHolder(new ReceiptTestCaseController(prescriptionSimulator, applicationContext)), "/old");
        handler.addServlet(new ServletHolder(new ReseptFormidlerLogTestController(prescriptionSimulator)), "/log");
        handler.addServlet(new ServletHolder(new PharmaTestCaseApiController(prescriptionSimulator, applicationContext)), "/api/*");

        return handler;
    }

    private Handler createOpsHandler() {
        WebAppContext handler = new WebAppContext(null, "/ops");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-ops-webapp"));

        handler.addServlet(new ServletHolder(logServlet), "/log/*");
        handler.addServlet(new ServletHolder(new IdCheckServlet()), "/idCheck");

        handler.addFilter(new FilterHolder(new AuthenticationFilter(authConfig)), "/idCheck", EnumSet.of(DispatcherType.REQUEST));

        return handler;
    }

    public URI getURL() {
        return server.getURI();
    }

    static JdbcHealthcareServiceRepository createHealthcareRepository() throws MalformedURLException {
        File arFile = new File("../data-dumps/AR.xml.gz");
        URL url;
        if (arFile.exists()) {
            url = arFile.toURI().toURL();
        } else {
            url = JdbcHealthcareServiceRepository.SEED_URL;
        }
        return new JdbcHealthcareServiceRepository(DataSources.createHealthcareServiceDataSource(), url);
    }

    public static PractitionerRepository createPractitionerRepository() throws IOException {
        File hprFile = new File("../data-dumps/HprExport.L3.csv.v2.zip");
        URL url;
        if (hprFile.exists()) {
            url = hprFile.toURI().toURL();
        } else {
            url = JdbcPractitionerRepository.SEED_URL;
        }
        PractitionerRepository practitionerRepository = new JdbcPractitionerRepository(
                DataSources.createPractitionerDataSource(),
                CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));
        practitionerRepository.refresh(url);
        return practitionerRepository;
    }

}
