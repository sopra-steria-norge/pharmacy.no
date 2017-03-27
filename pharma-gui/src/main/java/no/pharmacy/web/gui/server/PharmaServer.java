package no.pharmacy.web.gui.server;

import javax.sql.DataSource;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import no.pharmacy.dispense.JdbcMedicationDispenseRepository;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.infrastructure.logging.LogConfiguration;
import no.pharmacy.medication.FestMedicationImporter;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.web.dispense.DispenseOrderController;
import no.pharmacy.web.dispense.PharmacistController;
import no.pharmacy.web.dispense.PrescriptionsController;
import no.pharmacy.web.dispense.PrescriptionsSource;
import no.pharmacy.web.infrastructure.logging.LogDisplayServlet;
import no.pharmacy.web.test.FakeReseptFormidler;
import no.pharmacy.web.test.ReceiptTestCaseController;

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
        server.setHandler(createHandler());
        server.start();
    }

    private Handler createHandler() {
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ShutdownHandler(SHUTDOWN_TOKEN, true, true));

        DataSource medicationDataSource = createMedicationDataSource();
        DataSource pharmaDataSource = createPharmaDataSource();

        JdbcMedicationRepository medicationRepository = new JdbcMedicationRepository(medicationDataSource);
        medicationRepository.refresh(FestMedicationImporter.FEST_URL.toString());

        FakeReseptFormidler reseptFormidler = new FakeReseptFormidler(medicationRepository);

        handlers.addHandler(createOpsHandler());
        handlers.addHandler(createPharmaTestRig(reseptFormidler, medicationRepository));
        handlers.addHandler(createPharmaGui(reseptFormidler, pharmaDataSource, medicationRepository));

        return handlers;
    }

    private DataSource createPharmaDataSource() {
        JdbcConnectionPool dataSource = JdbcConnectionPool.create("jdbc:h2:file:./target/db/pharmacist", "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("db/db-pharmacist");
        flyway.migrate();

        return dataSource;
    }

    private JdbcConnectionPool createMedicationDataSource() {
        String jdbcUrl = "jdbc:h2:file:./target/db/medications";
        JdbcConnectionPool dataSource = JdbcConnectionPool.create(jdbcUrl, "sa", "");
        logger.info("Initializing {}", jdbcUrl);

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("db/db-medications");
        flyway.migrate();

        return dataSource;
    }

    private Handler createPharmaGui(PrescriptionsSource reseptFormidler, DataSource dataSource, MedicationRepository medicationRepository) {
        WebAppContext handler = new WebAppContext(null, "/");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-webapp"));

        MedicationDispenseRepository medicationDispenseRepository = new JdbcMedicationDispenseRepository(dataSource, medicationRepository);
        handler.addServlet(new ServletHolder(new PrescriptionsController(reseptFormidler, medicationDispenseRepository)), "/");
        handler.addServlet(new ServletHolder(new DispenseOrderController(medicationDispenseRepository, medicationRepository)), "/medicationDispenseCollections/*");
        handler.addServlet(new ServletHolder(new DispenseOrderController(medicationDispenseRepository, medicationRepository)), "/dispenseOrder/*");
        handler.addServlet(new ServletHolder(new PharmacistController(medicationDispenseRepository)), "/pharmacist/*");

        return handler;
    }

    private Handler createPharmaTestRig(FakeReseptFormidler reseptFormidler, MedicationRepository medicationRepository) {
        WebAppContext handler = new WebAppContext(null, "/pharma-test");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-testrig-webapp"));

        handler.addServlet(new ServletHolder(new ReceiptTestCaseController(reseptFormidler, medicationRepository)), "/");

        return handler;
    }

    private Handler createOpsHandler() {
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/ops");
        handler.addServlet(new ServletHolder(logServlet), "/log/*");
        return handler;
    }

}
