package no.pharmacy.patient;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.Test;

import no.pharmacy.core.Reference;
import no.pharmacy.test.MockPersonGateway;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class JdbcPatientRepositoryTest {

    private PharmaTestData testData = new PharmaTestData();
    private DataSource dataSource = TestDataSource.patientInstance();
    private MockPersonGateway personGateway = new MockPersonGateway();
    private JdbcPatientRepository repository = new JdbcPatientRepository(dataSource, personGateway);

    @Test
    public void shouldFindSavedPerson() throws Exception {
        String nationalId = testData.unusedNationalId();
        String patientName = PharmaTestData.sampleName();
        Reference patient = repository.savePatient(nationalId, patientName);
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

}
