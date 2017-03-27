package no.pharmacy.test;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDataSource {

    private static final Logger logger = LoggerFactory.getLogger(TestDataSource.class);

    private static DataSource medicationsDataSource;
    private static DataSource pharmacistDataSource;

    private static DataSource patientDataSource;

    public synchronized static DataSource medicationInstance() {
        if (medicationsDataSource == null) {
            String jdbcUrl = System.getProperty("test.medication.jdbc.url", "jdbc:h2:mem:medication");
            medicationsDataSource = JdbcConnectionPool.create(jdbcUrl, "sa", "");
            logger.info("Initializing {}", jdbcUrl);

            Flyway flyway = new Flyway();
            flyway.setDataSource(medicationsDataSource);
            flyway.setLocations("db/db-medications/");
            flyway.clean();
            flyway.migrate();
        }
        return medicationsDataSource;
    }

    public synchronized static DataSource pharmacistInstance() {
        if (pharmacistDataSource == null) {
            String jdbcUrl = System.getProperty("test.pharmacist.jdbc.url", "jdbc:h2:mem:pharmacist");
            pharmacistDataSource = JdbcConnectionPool.create(jdbcUrl, "sa", "");
            logger.info("Initializing {}", jdbcUrl);

            Flyway flyway = new Flyway();
            flyway.setDataSource(pharmacistDataSource);
            flyway.setLocations("db/db-pharmacist/");
            flyway.clean();
            flyway.migrate();
        }
        return pharmacistDataSource;
    }

    public static synchronized DataSource patientInstance() {
        if (patientDataSource == null) {
            patientDataSource = createDataSource("test.patient.jdbc.url", "jdbc:h2:mem:patient", "db/db-patient");
        }
        return patientDataSource;
    }

    private static DataSource createDataSource(String property, String jdbcDefaultUrl, String migrations) {
        String jdbcUrl = System.getProperty(property, jdbcDefaultUrl);
        DataSource dataSource = JdbcConnectionPool.create(jdbcUrl, "sa", "");
        logger.info("Initializing {}", jdbcUrl);

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(migrations);
        flyway.clean();
        flyway.migrate();
        return dataSource;
    }

}
