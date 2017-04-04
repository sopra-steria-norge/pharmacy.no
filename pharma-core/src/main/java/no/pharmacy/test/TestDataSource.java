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

    private static DataSource organizationsDataSource;

    public synchronized static DataSource medicationInstance() {
        if (medicationsDataSource == null) {
            // TODO: Inconsistent "medication" vs "medications"
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
            pharmacistDataSource = createMemDataSource("pharmacist");
        }
        return pharmacistDataSource;
    }

    public static synchronized DataSource patientInstance() {
        if (patientDataSource == null) {
            patientDataSource = createMemDataSource("patient");
        }
        return patientDataSource;
    }

    public static synchronized DataSource organizationsDataSource() {
        if (organizationsDataSource == null) {
            organizationsDataSource = createMemDataSource("organizations");
        }
        return organizationsDataSource;
    }


    public static DataSource createMemDataSource(String name) {
        return createDataSource("test." + name + ".jdbc.url", "jdbc:h2:mem:" + name, "db/db-" + name);
    }

    public static DataSource createFileDataSource(String name) {
        return createDataSource("test." + name + ".file.jdbc.url", "jdbc:h2:file:./target/db/" + name, "db/db-" + name);
    }

    public static DataSource createDataSource(String property, String jdbcDefaultUrl, String migrations) {
        String jdbcUrl = System.getProperty(property, jdbcDefaultUrl);
        DataSource dataSource = JdbcConnectionPool.create(jdbcUrl, "sa", "");
        logger.info("Initializing {}", jdbcUrl);

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(migrations);
        flyway.migrate();
        return dataSource;
    }



}
