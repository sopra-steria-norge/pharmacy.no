package no.pharmacy.practitioner;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.sql.DataSource;

import org.junit.Test;

import no.pharmacy.core.PersonReference;
import no.pharmacy.test.TestDataSource;

public class JdbcPractitionerRepositoryTest {

    @Test
    public void shouldImportHpr() throws Exception {
        JdbcPractitionerRepository repository = new JdbcPractitionerRepository(
                TestDataSource.createMemDataSource("practitioners"));

        repository.refresh("src/test/resources/hpr-mini/");

        assertThat(repository.listDoctors())
                .extracting(PersonReference::getReference)
                .doesNotContain("4514750")
                .contains("1002104");

        assertThat(repository.getAuthorizations("1002104"))
            .contains(PractionerAuthorization.DOCTOR)
            .doesNotContain(PractionerAuthorization.PHARMACIST);
        assertThat(repository.getAuthorizations("4514750"))
            .doesNotContain(PractionerAuthorization.DOCTOR)
            .contains(PractionerAuthorization.PHARMACIST);
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        DataSource dataSource = TestDataSource.createDataSource("test.practitioners.jdbc.url",
                "jdbc:h2:file:./target/db/practitioners",
                "db/db-practitioners");
        JdbcPractitionerRepository repository = new JdbcPractitionerRepository(dataSource);

//        repository.refresh("src/test/resources/hpr-mini/");
        repository.refresh("target/HprExport.L3.csv.v2.zip");
    }



}
