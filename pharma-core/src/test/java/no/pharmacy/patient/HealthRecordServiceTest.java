package no.pharmacy.patient;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Test;

import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.JdbcMedicationDispenseRepository;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.organization.JdbcHealthcareServiceRepository;
import no.pharmacy.patient.HealthRecordQuery.HealthRecordQueryPurpose;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class HealthRecordServiceTest {

    private DataSource dataSource = TestDataSource.pharmacistInstance();
    private PharmaTestData testData = new PharmaTestData();
    private JdbcHealthcareServiceRepository healthcareServiceRepository = new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource(), JdbcHealthcareServiceRepository.SEED_URL);
    private MedicationDispenseRepository repository = new JdbcMedicationDispenseRepository(
            dataSource,
            testData.getMedicationRepository(),
            healthcareServiceRepository);
    private PatientRepository patientRepository = new JdbcPatientRepository(TestDataSource.patientInstance(), s -> testData.samplePatient(), CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));
    private HealthRecordService healthRecordService = new HealthRecordService(repository, patientRepository);

    @Test
    public void shouldStoreQuery() throws Exception {
        HealthRecordQuery query = sampleQuery();

        assertThat(query).hasNoNullFieldsOrProperties();

        UUID queryId = healthRecordService.queryHealthRecord(query);
        assertThat(healthRecordService.retrieveQuery(queryId))
            .isEqualToComparingFieldByField(query);
    }

    @Test
    public void shouldExecuteQuery() throws Exception {
        DispenseOrder order = sampleCompletedMedicationDispenseOrder();
        DispenseOrder otherOrder = sampleCompletedMedicationDispenseOrder();

        HealthRecordQuery query = sampleQuery();
        query.setPatientId(UUID.fromString(order.getPatient().getReference()));
        assertThat(query).hasNoNullFieldsOrProperties();

        UUID queryId = healthRecordService.queryHealthRecord(query);

        assertThat(healthRecordService.listMedicationDispenses(queryId))
            .isNotEmpty()
            .contains(order)
            .doesNotContain(otherOrder);
    }

    private DispenseOrder sampleCompletedMedicationDispenseOrder() {
        MedicationOrder medicationOrder = testData.sampleMedicationOrder();
        DispenseOrder dispenseOrder = new DispenseOrder();
        dispenseOrder.setPatient(medicationOrder.getSubject());
        MedicationDispense dispense = dispenseOrder.addMedicationOrder(medicationOrder);
        repository.saveDispenseOrder(dispenseOrder);

        dispense.setMedication(medicationOrder.getMedication());
        dispense.setPrice(PharmaTestData.samplePrice());
        dispense.setConfirmedByPharmacist(true);
        dispense.setPackagingControlled(true);
        dispenseOrder.setDispensed();
        repository.update(dispenseOrder);

        return dispenseOrder;
    }

    private HealthRecordQuery sampleQuery() {
        HealthRecordQuery query = new HealthRecordQuery();
        query.setOrganizationHerNumber(testData.sampleHerNumber());
        query.setOperatorHprNumber(testData.samplePractitioner().getReference().getReference());
        query.setOperatorJwtToken("sdgnslnlsdg");
        query.setPatientId(UUID.randomUUID());
        query.setPurpose(HealthRecordQueryPurpose.PATIENT_REQUEST);
        query.setRequestorIdType(Optional.of("PASSPORT"));
        query.setRequestorIdNumber(Optional.of("12345678"));
        return query;
    }


}
