package no.pharmacy.organization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.pharmacy.test.TestDataSource;

public class JdbcHealthcareServiceRepositoryTest {

    @Test
    public void shouldReadPharmacies() throws Exception {
        JdbcHealthcareServiceRepository repository = new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource());

        repository.refresh(getClass().getResource("/seed/AR-mini.xml.gz"));
        repository.refresh(getClass().getResource("/seed/AR-mini.xml.gz"));

        assertThat(repository.listPharmacies())
            .extracting(HealthcareService::getDisplay)
            .containsOnlyOnce("BOOTS APOTEK SOLLI", "APOTEK 1 FINNSNES", "APOTEK 1 FINNSNES");

        assertThat(repository.retrieve("90416").getDisplay())
            .isEqualTo("BOOTS APOTEK SOLLI");

        assertThat(repository.listPharmacies().iterator().next())
            .hasNoNullFieldsOrProperties();
    }

    @Test
    public void shouldUpdatePharmacies() throws Exception {
        JdbcHealthcareServiceRepository repository = new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource());

        repository.refresh(getClass().getResource("/seed/AR-mini.xml.gz"));
        assertThat(repository.retrieve("90325").getDisplay())
            .isEqualTo("APOTEK 1 ÅKRA");

        repository.refresh(getClass().getResource("/AR-update.xml"));
        assertThat(repository.retrieve("90325").getDisplay())
            .isEqualTo("APOTEK 1 ÅKRA (endret)");
    }


}
