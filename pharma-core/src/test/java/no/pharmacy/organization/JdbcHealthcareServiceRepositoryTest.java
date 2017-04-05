package no.pharmacy.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.junit.Test;

import no.pharmacy.infrastructure.IOUtil;
import no.pharmacy.test.TestDataSource;

public class JdbcHealthcareServiceRepositoryTest {

    @Test
    public void shouldReadPharmacies() throws Exception {
        JdbcHealthcareServiceRepository repository = new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource());

        try(InputStream input = IOUtil.resource("seed/AR-mini.xml.gz")) {
            repository.refresh(input);
        }
        try(InputStream input = IOUtil.resource("seed/AR-mini.xml.gz")) {
            repository.refresh(input);
        }

        assertThat(repository.listPharmacies())
            .extracting(HealthcareService::getDisplay)
            .containsOnlyOnce("BOOTS APOTEK SOLLI", "APOTEK 1 FINNSNES", "APOTEK 1 FINNSNES");

        assertThat(repository.listPharmacies().iterator().next())
            .hasNoNullFieldsOrProperties();
    }

    @Test
    public void shouldUpdatePharmacies() throws Exception {
        JdbcHealthcareServiceRepository repository = new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource());

        try(InputStream input = IOUtil.resource("seed/AR-mini.xml.gz")) {
            repository.refresh(input);
        }
        assertThat(repository.getOrganization("90325").getDisplay())
            .isEqualTo("APOTEK 1 ÅKRA");

        try(InputStream input = IOUtil.resource("AR-update.xml")) {
            repository.refresh(input);
        }
        assertThat(repository.getOrganization("90325").getDisplay())
            .isEqualTo("APOTEK 1 ÅKRA (endret)");
    }


}
