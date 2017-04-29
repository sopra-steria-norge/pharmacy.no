package no.pharmacy.test;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class TestDataSource {

    private static final Logger logger = LoggerFactory.getLogger(TestDataSource.class);

    private static DataSource medicationsDataSource;
    private static DataSource pharmacistDataSource;

    private static DataSource patientDataSource;

    private static DataSource organizationsDataSource;

    private static DataSource practitionersDataSource;

    public synchronized static DataSource medicationInstance() {
        if (medicationsDataSource == null) {
            medicationsDataSource = createMemDataSource("medications");
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

    public static DataSource practitionersInstance() {
        if (practitionersDataSource == null) {
            practitionersDataSource = createMemDataSource("practitioners");
        }
        return practitionersDataSource;
    }

    public static synchronized DataSource organizationsDataSource() {
        if (organizationsDataSource == null) {
            organizationsDataSource = createMemDataSource("organizations");
        }
        return organizationsDataSource;
    }


    public static DataSource createMemDataSource(String name) {
        return createDataSource(System.getProperty("test." + name + ".jdbc.url", "jdbc:h2:mem:" + name), "db/db-" + name);
    }

    public static DataSource createFileDataSource(String name) {
        return createDataSource(System.getProperty("test." + name + ".file.jdbc.url", "jdbc:h2:file:./target/db/" + name), "db/db-" + name);
    }

    public static DataSource createDataSource(String jdbcUrl, String migrations) {
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



}
