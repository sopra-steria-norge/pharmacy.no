package no.pharmacy.order;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.Test;

import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.medication.Medication;
import no.pharmacy.test.JdbcMedicationDispenseRepository;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class MedicationDispenseRepositoryTest {

    private DataSource dataSource = TestDataSource.instance();
    private PharmaTestData testData = new PharmaTestData();
    private MedicationDispenseRepository repository = new JdbcMedicationDispenseRepository(dataSource, testData.getMedicationRepository());

    @Test
    public void retrievesSimpleDispenseOrder() {
        DispenseOrder order = new DispenseOrder();
        order.addMedicationOrder(testData.sampleMedicationOrder());

        repository.saveDispenseOrder(order);

        assertThat(order).hasNoNullFieldsOrProperties();
        DispenseOrder retrievedOrder = repository.getMedicationDispenseCollectionById(order.getIdentifier());
        assertThat(retrievedOrder)
            .isEqualToComparingFieldByField(order);
        assertThat(retrievedOrder.getMedicationOrders().get(0))
            .isEqualToIgnoringGivenFields(order.getMedicationOrders().get(0), "alternatives");
    }

    @Test
    public void populatesAlternativeMedicationsOnRetrieve() {
        DispenseOrder order = new DispenseOrder();
        Medication medication = testData.medicationWithSubstitutes();
        order.addMedicationOrder(testData.sampleMedicationOrder(medication));

        repository.saveDispenseOrder(order);
        MedicationOrder retrievedPrescription = repository.getMedicationDispenseCollectionById(order.getIdentifier()).getMedicationOrders().get(0);
        assertThat(retrievedPrescription.getAlternatives())
            .extracting(m -> m.getSubstitutionGroup())
            .containsOnly(medication.getSubstitutionGroup());
        assertThat(retrievedPrescription.getAlternatives().size())
            .isGreaterThan(1);
    }

    @Test
    public void populatesMedicationDispense() {
        DispenseOrder order = new DispenseOrder();

        MedicationOrder medicationOrder = testData.sampleMedicationOrder();
        order.addMedicationOrder(medicationOrder);

        assertThat(order.getMedicationDispenseList()).hasSize(1);
        MedicationDispense dispense = order.getMedicationDispenseList().get(0);
        assertThat(dispense.getMedication()).isNull();
        assertThat(dispense.getAuthorizingPrescription())
            .isEqualTo(medicationOrder);
    }

}
