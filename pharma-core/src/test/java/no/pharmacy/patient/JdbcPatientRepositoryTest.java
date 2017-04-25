package no.pharmacy.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;

import org.junit.Test;

import no.pharmacy.core.PersonReference;
import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.test.MockPersonGateway;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class JdbcPatientRepositoryTest {

    private PharmaTestData testData = new PharmaTestData();
    private DataSource dataSource = TestDataSource.patientInstance();
    private MockPersonGateway personGateway = new MockPersonGateway();
    private JdbcPatientRepository repository = new JdbcPatientRepository(dataSource, personGateway, createEncryptionKey());

    public static SecretKeySpec createEncryptionKey() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] key = PharmaTestData.lorum().getBytes();
            return new SecretKeySpec(Arrays.copyOf(sha.digest(key), 16), "AES");
        } catch (NoSuchAlgorithmException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    @Test
    public void shouldFindSavedPerson() throws Exception {
        String nationalId = testData.unusedNationalId();
        PersonReference samplePatient = testData.samplePatient();
        PersonReference patient = repository.savePatient(nationalId, samplePatient.getFirstName(), samplePatient.getLastName());
        assertThat(patient.getDisplay()).isEqualTo(samplePatient.getDisplay());
        assertThat(repository.findPatient(patient.getReference()))
            .isEqualToComparingFieldByField(patient);
        assertThat(repository.findPatientByNationalId(nationalId))
            .isEqualToComparingFieldByField(patient);
    }

    @Test
    public void shouldLookupUnregisteredPeople() {
        String nationalId = testData.unusedNationalId();
        PersonReference patientName = testData.samplePatient();
        personGateway.putPerson(nationalId, patientName);

        assertThat(repository.findPatientByNationalId(nationalId).getDisplay())
            .isEqualTo(patientName.getDisplay());
    }

    @Test
    public void shouldFindPeopleByName() {
        String nationalId = testData.unusedNationalId();

        PersonReference samplePatient = testData.samplePatient();
        PersonReference patient = repository.savePatient(nationalId, samplePatient.getFirstName(), samplePatient.getLastName());

        PersonQuery query = new PersonQuery();
        query.setFirstName(patient.getFirstName());
        query.setLastName(patient.getLastName());
        assertThat(repository.queryPatient(query))
            .contains(patient);

        query.setFirstName("This is not " + patient.getFirstName());
        assertThat(repository.queryPatient(query))
            .doesNotContain(patient);
    }

    public void shouldRejectIncompleteQueries() {
        PersonQuery query = new PersonQuery();
        assertThatThrownBy(() -> { repository.queryPatient(query); })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must contain");

        query.setLastName(testData.samplePatient().getLastName());
        assertThatThrownBy(() -> { repository.queryPatient(query); })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must contain");

        query.setLastName(null);
        query.setFirstName(testData.samplePatient().getFirstName());
        assertThatThrownBy(() -> { repository.queryPatient(query); })
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must contain");
    }

    @Test
    public void shouldFindPeopleByNationalId() {
        String nationalId = testData.unusedNationalId();

        PersonReference samplePatient = testData.samplePatient();
        PersonReference patient = repository.savePatient(nationalId, samplePatient.getFirstName(), samplePatient.getLastName());

        PersonQuery query = new PersonQuery();
        query.setNationalId(nationalId);
        assertThat(repository.queryPatient(query))
            .contains(patient);

        query.setNationalId(testData.unusedNationalId());
        assertThat(repository.queryPatient(query))
            .doesNotContain(patient);
    }


    @Test
    public void shouldDecodeNationalId() {
        String nationalId = testData.unusedNationalId();
        PersonReference samplePatient = testData.samplePatient();

        PersonReference patient = repository.savePatient(nationalId, samplePatient.getFirstName(), samplePatient.getLastName());
        assertThat(repository.lookupPatientNationalId(patient))
            .isEqualTo(nationalId);
    }

}
