package no.pharmacy.web.gui.server;

import java.io.File;

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

import ch.qos.logback.classic.Level;
import no.pharmacy.infrastructure.logging.LogConfiguration;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.order.MedicationDispenseRepository;
import no.pharmacy.test.JdbcMedicationDispenseRepository;
import no.pharmacy.web.prescriptions.DispenseOrderController;
import no.pharmacy.web.prescriptions.PharmacistController;
import no.pharmacy.web.prescriptions.PrescriptionsController;
import no.pharmacy.web.prescriptions.PrescriptionsSource;
import no.pharmacy.web.test.FakeReseptFormidler;
import no.pharmacy.web.test.ReceiptTestCaseController;

public class PharmaServer {

    private static final String SHUTDOWN_TOKEN = "c5d4a90d-e4be-414c-ad8a-2a172c31a230";

    private LogConfiguration logConfiguration = new LogConfiguration();
    private Server server;

    public PharmaServer() {
        logConfiguration.setLevel("org.eclipse.jetty", Level.WARN);
        logConfiguration.setLevel("org.flywaydb", Level.WARN);

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

        DataSource dataSource = createDataSource();

        JdbcMedicationRepository medicationRepository = new JdbcMedicationRepository(dataSource);
        medicationRepository.refresh();

        FakeReseptFormidler reseptFormidler = new FakeReseptFormidler(medicationRepository);

        handlers.addHandler(createPharmaTestRig(reseptFormidler, medicationRepository));
        handlers.addHandler(createPharmaGui(reseptFormidler, dataSource));

        return handlers;
    }

    private JdbcConnectionPool createDataSource() {
        File currentDir = new File(System.getProperty("user.dir"));
        File dbFile = new File(currentDir, "target/db/pharma");
        String url = "jdbc:h2:file:" + dbFile.getAbsolutePath();
        JdbcConnectionPool dataSource = JdbcConnectionPool.create(url, "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("db/db-medications");
        flyway.migrate();

        return dataSource;
    }

    private Handler createPharmaGui(PrescriptionsSource reseptFormidler, DataSource dataSource) {
        WebAppContext handler = new WebAppContext(null, "/");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-webapp"));

        MedicationRepository medicationRepository = new JdbcMedicationRepository(dataSource);
        MedicationDispenseRepository medicationDispenseRepository = new JdbcMedicationDispenseRepository(dataSource, medicationRepository);
        handler.addServlet(new ServletHolder(new PrescriptionsController(reseptFormidler, medicationDispenseRepository)), "/");
        handler.addServlet(new ServletHolder(new DispenseOrderController(medicationDispenseRepository, medicationRepository)), "/medicationDispenseCollections/*");
        handler.addServlet(new ServletHolder(new PharmacistController(medicationDispenseRepository, medicationRepository)), "/pharmacist/*");

        return handler;
    }

    private Handler createPharmaTestRig(FakeReseptFormidler reseptFormidler, MedicationRepository medicationRepository) {
        WebAppContext handler = new WebAppContext(null, "/pharma-test");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-testrig-webapp"));

        handler.addServlet(new ServletHolder(new ReceiptTestCaseController(reseptFormidler, medicationRepository)), "/");

        return handler;
    }

}
