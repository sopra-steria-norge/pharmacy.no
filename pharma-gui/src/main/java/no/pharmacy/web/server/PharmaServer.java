package no.pharmacy.web.server;

import javax.crypto.SecretKey;
import javax.sql.DataSource;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import no.pharmacy.dispense.JdbcMedicationDispenseRepository;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.infrastructure.logging.LogConfiguration;
import no.pharmacy.medication.FestMedicationImporter;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.patient.JdbcPatientRepository;
import no.pharmacy.test.FakePrescriptionGateway;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;
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

        server = new Server(8080);
    }

    public static void main(String[] args) throws Exception {
        new PharmaServer().start();
    }

    private void start() throws Exception {
        server.setHandler(createHandlers());
        server.start();
    }

    private Handler createHandlers() {
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ShutdownHandler(SHUTDOWN_TOKEN, true, true));

        DataSource medicationDataSource = createMedicationDataSource();

        JdbcMedicationRepository medicationRepository = new JdbcMedicationRepository(medicationDataSource);
        medicationRepository.refresh(FestMedicationImporter.FEST_URL.toString());

        FakeReseptFormidler reseptFormidler = new FakeReseptFormidler(medicationRepository);

        SecretKey secretKey = CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes());

        handlers.addHandler(createOpsHandler());
        handlers.addHandler(createPharmaTestRig(reseptFormidler, medicationRepository));

        PharmaGuiHandler guiHandler = new PharmaGuiHandler();
        guiHandler.setMedicationRepository(medicationRepository);
        guiHandler.setPatientRepository(new JdbcPatientRepository(createPatientDataSource(), s -> PharmaTestData.sampleName(), secretKey));
        guiHandler.setPrescriptionGateway(new FakePrescriptionGateway(reseptFormidler, medicationRepository));
        guiHandler.setRepository(new JdbcMedicationDispenseRepository(createPharmaDataSource(), medicationRepository));

        handlers.addHandler(guiHandler.createHandler());

        return handlers;
    }

    private DataSource createPharmaDataSource() {
        return createDataSource("jdbc:h2:file:./target/db/pharmacist", "db/db-pharmacist");
    }

    private DataSource createPatientDataSource() {
        return createDataSource("jdbc:h2:file:./target/db/patient", "db/db-patient");
    }

    private DataSource createMedicationDataSource() {
        return createDataSource("jdbc:h2:file:./target/db/medications", "db/db-medications");
    }

    private DataSource createDataSource(String jdbcUrl, String migrations) {
        JdbcConnectionPool dataSource = JdbcConnectionPool.create(jdbcUrl, "sa", "");
        logger.info("Initializing {}", jdbcUrl);

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(migrations);
        flyway.migrate();

        return dataSource;
    }

    private Handler createPharmaTestRig(FakeReseptFormidler reseptFormidler, MedicationRepository medicationRepository) {
        WebAppContext handler = new WebAppContext(null, "/pharma-test");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-testrig-webapp"));

        handler.addServlet(new ServletHolder(new ReceiptTestCaseController(reseptFormidler, medicationRepository)), "/");
        handler.addServlet(new ServletHolder(new ReseptFormidlerLogTestController(reseptFormidler)), "/log");

        return handler;
    }

    private Handler createOpsHandler() {
        WebAppContext handler = new WebAppContext(null, "/ops");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-ops-webapp"));

        handler.addServlet(new ServletHolder(logServlet), "/log/*");
        return handler;
    }

}
