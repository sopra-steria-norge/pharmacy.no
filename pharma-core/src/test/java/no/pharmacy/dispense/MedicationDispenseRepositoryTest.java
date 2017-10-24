package no.pharmacy.dispense;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.Test;

import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.JdbcMedicationDispenseRepository;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationDispenseAction;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.medication.Medication;
import no.pharmacy.organization.JdbcHealthcareServiceRepository;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class MedicationDispenseRepositoryTest {

    private DataSource dataSource = TestDataSource.pharmacistInstance();
    private PharmaTestData testData = new PharmaTestData();
    private JdbcHealthcareServiceRepository healthcareServiceRepository = new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource(), JdbcHealthcareServiceRepository.SEED_URL);
    private MedicationDispenseRepository repository = new JdbcMedicationDispenseRepository(
            dataSource,
            testData.getMedicationRepository(),
            healthcareServiceRepository);

    @Test
    public void shouldRetrieveSimpleDispenseOrder() {
        DispenseOrder order = new DispenseOrder();
        MedicationDispense dispense = order.addMedicationOrder(testData.sampleMedicationOrder());
        dispense.setPrice(PharmaTestData.samplePrice());
        dispense.setPrintedDosageText("Corrected text");

        order.setDispensingOrganization(testData.sampleHealthcareService());
        order.setPatient(testData.samplePatient());
        order.setCustomerSignature(testData.samplePng());

        repository.saveDispenseOrder(order);

        assertThat(order).hasNoNullFieldsOrPropertiesExcept("dateDispensed");
        DispenseOrder retrievedOrder = repository.getDispenseOrderById(order.getIdentifier());
        assertThat(retrievedOrder)
            .isEqualToComparingFieldByField(order);
        assertThat(retrievedOrder.getMedicationOrders().get(0))
            .hasNoNullFieldsOrProperties()
            .isEqualToIgnoringGivenFields(order.getMedicationOrders().get(0), "alternatives");
        assertThat(retrievedOrder.getMedicationDispenses().get(0))
            .isEqualToComparingFieldByField(dispense)
            .hasNoNullFieldsOrPropertiesExcept("medication", "scannedGtin", "scannedDosageLabel", "dateDispensed");
    }

    @Test
    public void shouldPopulateAlternativeMedicationsOnRetrieve() {
        DispenseOrder order = new DispenseOrder();
        order.setPatient(testData.samplePatient());

        Medication medication = testData.medicationWithSubstitutes();
        order.addMedicationOrder(testData.sampleMedicationOrder(medication));

        repository.saveDispenseOrder(order);
        MedicationOrder retrievedPrescription = repository.getDispenseOrderById(order.getIdentifier()).getMedicationOrders().get(0);
        assertThat(retrievedPrescription.getAlternatives())
            .extracting(m -> m.getSubstitutionGroup())
            .containsOnly(medication.getSubstitutionGroup());
        assertThat(retrievedPrescription.getAlternatives().size())
            .isGreaterThan(1);
    }

    @Test
    public void shouldIncludeMedicationInAlternatives() {
        DispenseOrder order = new DispenseOrder();
        order.setPatient(testData.samplePatient());

        Medication medication = testData.medicationWithoutSubstitutes();
        order.addMedicationOrder(testData.sampleMedicationOrder(medication));
        repository.saveDispenseOrder(order);
        MedicationOrder retrievedPrescription = repository.getDispenseOrderById(order.getIdentifier()).getMedicationOrders().get(0);
        assertThat(retrievedPrescription.getAlternatives()).containsOnly(medication);

    }

    @Test
    public void shouldPopulateMedicationDispense() {
        DispenseOrder order = new DispenseOrder();

        MedicationOrder medicationOrder = testData.sampleMedicationOrder();
        assertThat(medicationOrder)
            .hasNoNullFieldsOrPropertiesExcept("id", "alternatives");
        order.addMedicationOrder(medicationOrder);

        assertThat(order.getMedicationDispenses()).hasSize(1);
        MedicationDispense dispense = order.getMedicationDispenses().get(0);
        assertThat(dispense.getMedication()).isNull();
        assertThat(dispense.getAuthorizingPrescription())
            .isEqualTo(medicationOrder);
    }

    @Test
    public void shouldUpdateMedicationDispense() {
        DispenseOrder order = new DispenseOrder();
        order.setPatient(testData.samplePatient());

        MedicationOrder medicationOrder = testData.sampleMedicationOrder();
        order.addMedicationOrder(medicationOrder);
        repository.saveDispenseOrder(order);

        MedicationDispense dispense = order.getMedicationDispenses().get(0);
        dispense.setPrice(PharmaTestData.samplePrice());
        dispense.setMedication(medicationOrder.getMedication());
        dispense.setPrintedDosageText("Updated dosage text");
        repository.update(dispense);

        DispenseOrder retrieved = repository.getDispenseOrderById(order.getIdentifier());
        assertThat(retrieved.getMedicationDispenses().get(0))
            .isEqualToComparingFieldByField(dispense);
    }

    @Test
    public void shouldSaveInteractionWarnings() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        dispenseOrder.setPatient(testData.samplePatient());
        Medication ritalin = testData.getMedication("500595");
        Medication aurorix = testData.getMedication("466813");
        MedicationDispense ritalinDispense = dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(ritalin));
        ritalinDispense.setMedication(ritalin);
        dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(aurorix)).setMedication(aurorix);

        repository.saveDispenseOrder(dispenseOrder);

        dispenseOrder.createWarnings();
        repository.update(ritalinDispense);

        MedicationDispense retrieved = repository.getDispenseOrderById(dispenseOrder.getIdentifier())
            .getMedicationDispenses().get(0);
        assertThat(retrieved).isEqualToComparingFieldByField(ritalinDispense);
    }

    @Test
    public void shouldUpdateDispenseActions() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        dispenseOrder.setPatient(testData.samplePatient());
        Medication ritalin = testData.getMedication("500595");
        Medication aurorix = testData.getMedication("466813");
        MedicationDispense ritalinDispense = dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(ritalin));
        ritalinDispense.setMedication(ritalin);
        dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(aurorix)).setMedication(aurorix);

        repository.saveDispenseOrder(dispenseOrder);

        dispenseOrder.createWarnings();
        repository.update(ritalinDispense);
        for (MedicationDispenseAction action : ritalinDispense.getWarningActions()) {
            action.setAction("2");
            action.setRemark("Test remark");
        }
        ritalinDispense.setConfirmedByPharmacist(true);
        ritalinDispense.setPackagingControlled(true);
        repository.update(ritalinDispense);

        DispenseOrder retrievedDispenseOrder = repository.getDispenseOrderById(dispenseOrder.getIdentifier());
        assertThat(retrievedDispenseOrder.getMedicationDispenses().get(0))
            .isEqualToComparingFieldByField(ritalinDispense);
    }

    @Test
    public void shouldDispenseOrder() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        dispenseOrder.setPatient(testData.samplePatient());
        dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(testData.sampleMedication()));

        repository.saveDispenseOrder(dispenseOrder);
        assertThat(repository.getDispenseOrderById(dispenseOrder.getIdentifier()))
            .isEqualToComparingFieldByField(dispenseOrder);

        MedicationDispense dispense = dispenseOrder.getDispenses().get(0);
        dispense.setMedication(dispense.getAuthorizingPrescription().getMedication());
        dispense.setPrice(PharmaTestData.samplePrice());

        repository.update(dispense);
        assertThat(repository.getDispenseOrderById(dispenseOrder.getIdentifier()))
            .isEqualToComparingFieldByField(dispenseOrder);

        dispense.setConfirmedByPharmacist(true);
        dispense.setPackagingControlled(true);

        assertThat(dispenseOrder.isReadyToDispense()).isTrue();

        dispenseOrder.setCustomerSignature(testData.samplePng());
        dispenseOrder.setDispensed();

        repository.update(dispenseOrder);

        DispenseOrder competedOrder = repository.getDispenseOrderById(dispenseOrder.getIdentifier());
        assertThat(competedOrder)
            .isEqualToComparingFieldByField(dispenseOrder);
        assertThat(competedOrder.getDispenses().get(0))
            .hasNoNullFieldsOrPropertiesExcept("scannedGtin", "scannedDosageLabel")
            .isEqualToComparingFieldByField(dispense);
    }

}
