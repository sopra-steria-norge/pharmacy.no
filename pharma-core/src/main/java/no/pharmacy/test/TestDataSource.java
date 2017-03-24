package no.pharmacy.test;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;

public class TestDataSource {

    private static DataSource medicationsDataSource;
    private static DataSource pharmacistDataSource;

    public synchronized static DataSource medicationInstance() {
        if (medicationsDataSource == null) {
            medicationsDataSource = JdbcConnectionPool.create("jdbc:h2:mem:medications", "sa", "");
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
            pharmacistDataSource = JdbcConnectionPool.create("jdbc:h2:mem:medications", "sa", "");
            Flyway flyway = new Flyway();
            flyway.setDataSource(pharmacistDataSource);
            flyway.setLocations("db/db-pharmacist/");
            flyway.clean();
            flyway.migrate();
        }
        return pharmacistDataSource;
    }

}
