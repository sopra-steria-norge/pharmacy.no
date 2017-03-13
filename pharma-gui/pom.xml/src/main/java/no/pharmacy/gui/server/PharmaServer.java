package no.pharmacy.gui.server;

import java.io.File;
import java.sql.Connection;

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

import no.pharmacy.gui.server.prescriptions.DispenseOrderController;
import no.pharmacy.gui.server.prescriptions.PrescriptionsController;
import no.pharmacy.gui.server.prescriptions.PrescriptionsSource;
import no.pharmacy.gui.server.test.FakeReseptFormidler;
import no.pharmacy.gui.server.test.ReceiptTestCaseController;
import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.medication.DownloadMedicationsFromFest;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.order.MedicationDispenseRepository;
import no.pharmacy.test.FakeMedicationDispenseRepository;

public class PharmaServer {

    private static final String SHUTDOWN_TOKEN = "c5d4a90d-e4be-414c-ad8a-2a172c31a230";
    private Server server = new Server(8080);

    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("user.dir"));
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
        if (medicationRepository.isEmpty()) {
            try (Connection conn = dataSource.getConnection()) {
                new DownloadMedicationsFromFest(medicationRepository).downloadFestFile(conn);
            } catch (Exception e) {
                throw ExceptionUtil.softenException(e);
            }
        }
        FakeReseptFormidler reseptFormidler = new FakeReseptFormidler(medicationRepository);

        handlers.addHandler(createPharmaTestRig(reseptFormidler, medicationRepository));
        handlers.addHandler(createPharmaGui(reseptFormidler, medicationRepository));

        return handlers;
    }

    private JdbcConnectionPool createDataSource() {
        File currentDir = new File(System.getProperty("user.dir"));
        File dbFile = new File(currentDir, "target/db/medications");
        String url = "jdbc:h2:file:" + dbFile.getAbsolutePath();
        System.out.println(url);
        JdbcConnectionPool dataSource = JdbcConnectionPool.create(url, "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("db/db-medications");
        flyway.migrate();

        return dataSource;
    }

    private Handler createPharmaGui(PrescriptionsSource reseptFormidler, MedicationRepository medicationRepository) {
        WebAppContext handler = new WebAppContext(null, "/");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-webapp"));

        MedicationDispenseRepository medicationDispenseRepository = new FakeMedicationDispenseRepository();
        handler.addServlet(new ServletHolder(new PrescriptionsController(reseptFormidler, medicationDispenseRepository)), "/");
        handler.addServlet(new ServletHolder(new DispenseOrderController(reseptFormidler, medicationDispenseRepository, medicationRepository)), "/medicationDispenseCollections/*");

        return handler;
    }

    private Handler createPharmaTestRig(FakeReseptFormidler reseptFormidler, MedicationRepository medicationRepository) {
        WebAppContext handler = new WebAppContext(null, "/pharma-test");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-testrig-webapp"));

        handler.addServlet(new ServletHolder(new ReceiptTestCaseController(reseptFormidler, medicationRepository)), "/");

        return handler;
    }

}
