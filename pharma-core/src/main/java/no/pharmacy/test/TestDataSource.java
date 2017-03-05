package no.pharmacy.test;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;

public class TestDataSource {

    private static DataSource dataSource;

    static {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:medications", "sa", "");
        Flyway flyway = new Flyway();

        flyway.setDataSource(dataSource);
        flyway.setLocations("db/db-medications/");

        flyway.clean();
        flyway.migrate();
    }

    public static DataSource instance() {
        return dataSource;
    }

}
