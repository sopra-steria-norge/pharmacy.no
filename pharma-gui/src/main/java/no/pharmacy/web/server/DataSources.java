package no.pharmacy.web.server;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSources {

    private static final Logger logger = LoggerFactory.getLogger(DataSources.class);

    static DataSource createHealthcareServiceDataSource() {
        return createDataSource("jdbc:h2:file:./target/db/organizations", "db/db-organizations");
    }

    static DataSource createPharmaDataSource() {
        return createLocalFileDataSource("pharmacist");
    }

    static DataSource createPatientDataSource() {
        return createLocalFileDataSource("patient");
    }

    public static DataSource createMedicationDataSource() {
        return createFileDataSource("medications");
    }

    static DataSource createPractitionerDataSource() {
        return createFileDataSource("practitioners");
    }

    private static DataSource createFileDataSource(String name) {
        return createDataSource("jdbc:h2:file:./target/db/" + name, "db/db-" + name);
    }

    static DataSource createLocalFileDataSource(String name) {
        return createDataSource("jdbc:h2:file:./target/db-local/" + name, "db/db-" + name);
    }

    private static DataSource createDataSource(String jdbcUrl, String migrations) {
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
