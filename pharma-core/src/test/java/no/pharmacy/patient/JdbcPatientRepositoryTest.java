package no.pharmacy.patient;

import static org.assertj.core.api.Assertions.assertThat;

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
        String patientName = PharmaTestData.sampleName();
        PersonReference patient = repository.savePatient(nationalId, patientName);
        assertThat(patient.getDisplay()).isEqualTo(patientName);
        assertThat(repository.findPatient(patient.getReference()))
            .isEqualToComparingFieldByField(patient);
        assertThat(repository.findPatientByNationalId(nationalId))
            .isEqualToComparingFieldByField(patient);
    }

    @Test
    public void shouldLookupUnregisteredPeople() {
        String nationalId = testData.unusedNationalId();
        String patientName = PharmaTestData.sampleName();
        personGateway.putPerson(nationalId, patientName);

        assertThat(repository.findPatientByNationalId(nationalId).getDisplay())
            .isEqualTo(patientName);
    }

    @Test
    public void shouldDecodeNationalId() {
        String nationalId = testData.unusedNationalId();
        String patientName = PharmaTestData.sampleName();

        PersonReference patient = repository.savePatient(nationalId, patientName);
        assertThat(repository.lookupPatientNationalId(patient))
            .isEqualTo(nationalId);
    }

}
