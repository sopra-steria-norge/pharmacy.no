package no.pharmacy.web.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.sql.DataSource;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.qos.logback.classic.Level;
import no.pharmacy.dispense.JdbcMedicationDispenseRepository;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.infrastructure.logging.LogConfiguration;
import no.pharmacy.medication.FestMedicationImporter;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.medicationorder.RFPrescriptionGateway;
import no.pharmacy.organization.HealthcareServiceRepository;
import no.pharmacy.organization.JdbcHealthcareServiceRepository;
import no.pharmacy.patient.JdbcPatientRepository;
import no.pharmacy.patient.PatientRepository;
import no.pharmacy.practitioner.JdbcPractitionerRepository;
import no.pharmacy.practitioner.PractitionerRepository;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;
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

    public PharmaServer() {
        logConfiguration.setLevel("org.eclipse.jetty", Level.WARN);
        logConfiguration.setLevel("org.flywaydb", Level.WARN);
        logServlet = new LogDisplayServlet(logConfiguration.getContext());

        int port = 8080;
        if (System.getenv("HTTP_PORT") != null) {
            port = Integer.parseInt(System.getenv("HTTP_PORT"));
        }
        server = new Server(port);
        server.addLifeCycleListener(Server.STOP_ON_FAILURE);
    }

    public static void main(String[] args) throws Exception {
        new PharmaServer().start();
    }

    private void start() throws Exception {
        server.setHandler(createHandlers());
        server.start();
        logger.warn("Started http://localhost:{}", server.getURI().getPort());
    }

    private Handler createHandlers() throws IOException, URISyntaxException {
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ShutdownHandler(SHUTDOWN_TOKEN, true, true));

        PatientRepository patientRepository = new JdbcPatientRepository(createPatientDataSource(), s -> PharmaTestData.sampleName(), CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));
        MedicationRepository medicationRepository = createMedicationRepository();
        PractitionerRepository practitionerRepository = createPractitionerRepository();
        HealthcareServiceRepository healthcareServiceRepository = createHealthcareServiceRepository();

        FakeReseptFormidler reseptFormidler = new FakeReseptFormidler(medicationRepository, patientRepository);

        handlers.addHandler(createOpsHandler());
        handlers.addHandler(createPharmaTestRig(reseptFormidler, medicationRepository, practitionerRepository));

        PharmaGuiHandler guiHandler = new PharmaGuiHandler();
        guiHandler.setMedicationRepository(medicationRepository);
        guiHandler.setPatientRepository(patientRepository);
        guiHandler.setHealthcareServiceRepository(healthcareServiceRepository);
        guiHandler.setPrescriptionGateway(new RFPrescriptionGateway(reseptFormidler, medicationRepository, patientRepository));
        guiHandler.setRepository(new JdbcMedicationDispenseRepository(createPharmaDataSource(), medicationRepository));

        handlers.addHandler(guiHandler.createHandler());

        return handlers;
    }

    private HealthcareServiceRepository createHealthcareServiceRepository() throws IOException, FileNotFoundException {
        HealthcareServiceRepository healthcareServiceRepository = new JdbcHealthcareServiceRepository(createHealthcareServiceDataSource());
        File arFile = new File("../data-dumps/AR.xml.gz");
        if (arFile.exists()) {
            healthcareServiceRepository.refresh(arFile.toURI().toURL());
        } else {
            healthcareServiceRepository.refresh(getClass().getResource("/seed/AR-mini.xml.gz"));
        }
        return healthcareServiceRepository;
    }

    private JdbcMedicationRepository createMedicationRepository() {
        JdbcMedicationRepository medicationRepository = new JdbcMedicationRepository(createMedicationDataSource());
        // TODO: Implement with lastmodified timestamp and checksum
        // TODO: Implement with timestamp entry checking
        medicationRepository.refresh(FestMedicationImporter.FEST_URL.toString());
        return medicationRepository;
    }

    private PractitionerRepository createPractitionerRepository() throws IOException {
        PractitionerRepository practitionerRepository = new JdbcPractitionerRepository(createPractitionerDataSource(),
                CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));
        File hprFile = new File("../data-dumps/HprExport.L3.csv.v2.zip");
        if (hprFile.exists()) {
            practitionerRepository.refresh(hprFile.toURI().toURL());
        } else {
            practitionerRepository.refresh(getClass().getResource("/seed/hpr-mini/"));
        }
        return practitionerRepository;
    }

    private DataSource createHealthcareServiceDataSource() {
        return createDataSource("jdbc:h2:file:./target/db/organizations", "db/db-organizations");
    }

    private DataSource createPharmaDataSource() {
        return createFileDataSource("pharmacist");
    }

    private DataSource createPatientDataSource() {
        return createFileDataSource("patient");
    }

    private DataSource createMedicationDataSource() {
        return createFileDataSource("medications");
    }

    private DataSource createPractitionerDataSource() {
        return createFileDataSource("practitioners");
    }

    private DataSource createFileDataSource(String name) {
        return createDataSource("jdbc:h2:file:./target/db/" + name, "db/db-" + name);
    }

    private DataSource createDataSource(String jdbcUrl, String migrations) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl); // config.setJdbcUrlFromProperty(property, jdbcDefault);
        config.setUsername("sa");
        config.setPassword("");

        DataSource dataSource = new HikariDataSource(config);
        logger.info("Initializing {}", jdbcUrl);

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(migrations);
        flyway.migrate();

        return dataSource;
    }

    private Handler createPharmaTestRig(FakeReseptFormidler reseptFormidler, MedicationRepository medicationRepository, PractitionerRepository practitionerRepository) {
        WebAppContext handler = new WebAppContext(null, "/pharma-test");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-testrig-webapp"));
        // Avoid locking files on disk
        handler.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");

        handler.addServlet(new ServletHolder(new ReceiptTestCaseController(reseptFormidler, medicationRepository, practitionerRepository)), "/old");
        handler.addServlet(new ServletHolder(new ReseptFormidlerLogTestController(reseptFormidler)), "/log");
        handler.addServlet(new ServletHolder(new PharmaTestCaseApiController(reseptFormidler, medicationRepository, practitionerRepository)), "/api/*");

        return handler;
    }

    private Handler createOpsHandler() {
        WebAppContext handler = new WebAppContext(null, "/ops");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-ops-webapp"));

        handler.addServlet(new ServletHolder(logServlet), "/log/*");

        handler.addServlet(new ServletHolder(new IdCheckServlet()), "/idCheck");

        handler.addFilter(new FilterHolder(new AuthenticationFilter(new AuthenticationConfiguration())), "/idCheck", EnumSet.of(DispatcherType.REQUEST));


        return handler;
    }

}
