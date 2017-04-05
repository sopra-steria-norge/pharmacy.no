package no.pharmacy.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.zip.GZIPInputStream;

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
import no.pharmacy.infrastructure.IOUtil;
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

        JdbcMedicationRepository medicationRepository = new JdbcMedicationRepository(createMedicationDataSource());
        medicationRepository.refresh(FestMedicationImporter.FEST_URL.toString());

        PractitionerRepository practitionerRepository = new JdbcPractitionerRepository(createPractitionerDataSource(),
                CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));
        File hprFile = new File("../data-dumps/HprExport.L3.csv.v2.zip");
        if (hprFile.exists()) {
            practitionerRepository.refresh(hprFile.getPath());
        } else {
            practitionerRepository.refresh("classpath:seed/hpr-mini/");
        }

        HealthcareServiceRepository healthcareServiceRepository = new JdbcHealthcareServiceRepository(createHealthcareServiceDataSource());
        File arFile = new File("../data-dumps/AR.xml.gz");
        if (arFile.exists()) {
            try(InputStream input = new GZIPInputStream(new FileInputStream(arFile), 16*1024*1024)) {
                healthcareServiceRepository.refresh(input);
            }
        } else {
            try(InputStream input = IOUtil.resource("seed/AR-mini.xml.gz")) {
                healthcareServiceRepository.refresh(input);
            }
        }

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

        handler.addServlet(new ServletHolder(new ReceiptTestCaseController(reseptFormidler, medicationRepository, practitionerRepository)), "/");
        handler.addServlet(new ServletHolder(new ReseptFormidlerLogTestController(reseptFormidler)), "/log");

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
