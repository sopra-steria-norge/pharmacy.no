package no.pharmacy.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.sql.DataSource;

import org.junit.Test;

import no.pharmacy.test.TestDataSource;

public class JdbcHealthcareServiceRepositoryTest {

    @Test
    public void shouldReadPharmacies() throws Exception {
        JdbcHealthcareServiceRepository repository = new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource());

        repository.refresh("src/test/resources/AR-mini.xml");
        repository.refresh("src/test/resources/AR-mini.xml");

        assertThat(repository.listPharmacies())
            .extracting(HealthcareService::getDisplay)
            .containsOnlyOnce("BOOTS APOTEK SOLLI", "APOTEK 1 FINNSNES", "APOTEK 1 FINNSNES");

        assertThat(repository.listPharmacies().iterator().next())
            .hasNoNullFieldsOrProperties();
    }

    @Test
    public void shouldUpdatePharmacies() throws Exception {
        JdbcHealthcareServiceRepository repository = new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource());

        repository.refresh("src/test/resources/AR-mini.xml");
        assertThat(repository.getOrganization("90325").getDisplay())
            .isEqualTo("APOTEK 1 ÅKRA");

        repository.refresh("src/test/resources/AR-update.xml");
        assertThat(repository.getOrganization("90325").getDisplay())
            .isEqualTo("APOTEK 1 ÅKRA (endret)");
    }



    public static void main(String[] args) throws IOException {
        DataSource dataSource = TestDataSource.createDataSource("test.organizations.jdbc.url",
                "jdbc:h2:file:./target/db/organizations",
                "db/db-practitioners");
        JdbcHealthcareServiceRepository repository = new JdbcHealthcareServiceRepository(dataSource);
        repository.refresh("../pharma-gui/target/AR-mini.xml");
    }

}
