package no.pharmacy.practitioner;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import no.pharmacy.core.PersonReference;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.test.TestDataSource;

public class JdbcPractitionerRepositoryTest {

    private JdbcPractitionerRepository repository = new JdbcPractitionerRepository(
            TestDataSource.practitionersInstance(),
            CryptoUtil.aesKey("sdgsdg lsdngdlsdnglsndg".getBytes()));

    @Test
    public void shouldImportHpr() throws Exception {
        repository.refresh(JdbcPractitionerRepository.SEED_URL);

        assertThat(repository.listDoctors())
                .extracting(PersonReference::getReference)
                .doesNotContain("4514750")
                .contains("1002104");

        assertThat(repository.getAuthorizations(1002104))
            .contains(PractionerAuthorization.DOCTOR)
            .doesNotContain(PractionerAuthorization.PHARMACIST);
        assertThat(repository.getAuthorizations(4514750))
            .doesNotContain(PractionerAuthorization.DOCTOR)
            .contains(PractionerAuthorization.PHARMACIST);
    }

    @Test
    public void shouldOnlyImportAuthorizationsOnce() throws Exception {
        repository.refresh(JdbcPractitionerRepository.SEED_URL);
        repository.refresh(JdbcPractitionerRepository.SEED_URL);
        assertThat(repository.getAuthorizations(1002104))
            .containsOnlyOnce(PractionerAuthorization.DOCTOR);
    }

    @Test
    public void shouldImportUpdates() throws IOException {
        repository.refresh(JdbcPractitionerRepository.SEED_URL);

        Practitioner practitioner = repository.getPractitioner("1002104").get();
        assertThat(practitioner).hasNoNullFieldsOrProperties();
        assertThat(practitioner.getDisplay())
            .isEqualTo("RUDI JOHAN RAUDI");
        assertThat(practitioner.getAuthorizations())
            .contains(PractionerAuthorization.DOCTOR)
            .doesNotContain(PractionerAuthorization.PHARMACIST);

        assertThat(repository.getPractitioner("9600000")).isEmpty();

        repository.refresh(getClass().getResource("/hpr-update/"));
        assertThat(repository.getPractitioner("1002104").get().getDisplay())
            .isEqualTo("NOEN ANDRE RAUDI");
        assertThat(repository.getAuthorizations(1002104))
            .doesNotContain(PractionerAuthorization.DOCTOR)
            .contains(PractionerAuthorization.PHARMACIST);

        assertThat(repository.getPractitioner("9600000")).isNotEmpty();
    }
}
