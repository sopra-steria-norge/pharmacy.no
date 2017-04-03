package no.pharmacy.practitioner;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.sql.DataSource;

import org.junit.Test;

import no.pharmacy.core.PersonReference;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.test.TestDataSource;

public class JdbcPractitionerRepositoryTest {

    private JdbcPractitionerRepository repository = new JdbcPractitionerRepository(
            TestDataSource.createMemDataSource("practitioners"),
            CryptoUtil.aesKey("sdgsdg lsdngdlsdnglsndg".getBytes()));

    @Test
    public void shouldImportHpr() throws Exception {
        repository.refresh("src/test/resources/hpr-mini/");

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
        repository.refresh("src/test/resources/hpr-mini/");
        repository.refresh("src/test/resources/hpr-mini/");

        assertThat(repository.getAuthorizations(1002104))
            .containsOnlyOnce(PractionerAuthorization.DOCTOR);
    }

    @Test
    public void shouldImportUpdates() {
        repository.refresh("src/test/resources/hpr-mini/");

        Practitioner practitioner = repository.getPractitioner("1002104").get();
        assertThat(practitioner).hasNoNullFieldsOrProperties();
        assertThat(practitioner.getDisplay())
            .isEqualTo("RUDI JOHAN RAUDI");
        assertThat(practitioner.getAuthorizations())
            .contains(PractionerAuthorization.DOCTOR)
            .doesNotContain(PractionerAuthorization.PHARMACIST);

        assertThat(repository.getPractitioner("9600000")).isEmpty();

        repository.refresh("src/test/resources/hpr-update/");
        assertThat(repository.getPractitioner("1002104").get().getDisplay())
            .isEqualTo("NOEN ANDRE RAUDI");
        assertThat(repository.getAuthorizations(1002104))
            .doesNotContain(PractionerAuthorization.DOCTOR)
            .contains(PractionerAuthorization.PHARMACIST);

        assertThat(repository.getPractitioner("9600000")).isNotEmpty();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        DataSource dataSource = TestDataSource.createDataSource("test.practitioners.jdbc.url",
                "jdbc:h2:file:./target/db/practitioners",
                "db/db-practitioners");
        JdbcPractitionerRepository repository = new JdbcPractitionerRepository(dataSource,
                CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));

//        repository.refresh("src/test/resources/hpr-mini/");
        repository.refresh("target/HprExport.L3.csv.v2.zip");
    }



}
