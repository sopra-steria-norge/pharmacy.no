package no.pharmacy.order;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.medication.Medication;
import no.pharmacy.test.PharmaTestData;

public class DispenseOrderTest {

    private PharmaTestData testData = new PharmaTestData();

    @Test
    public void shouldListDrugInteractions() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        Medication ritalin = testData.sampleMedication("500595");
        Medication aurorix = testData.sampleMedication("466813");
        MedicationDispense ritalinDispense = dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(ritalin));
        ritalinDispense.setMedication(ritalin);
        dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(aurorix)).setMedication(aurorix);

        Assertions.assertThat(ritalinDispense.getWarnings(dispenseOrder))
            .isNotEmpty();
    }

    @Test
    public void shouldNotGiveInteractionsOnSimpleOrder() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        Medication ritalin = testData.sampleMedication("500595");
        MedicationDispense ritalinDispense = dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(ritalin));
        ritalinDispense.setMedication(ritalin);

        Assertions.assertThat(ritalinDispense.getWarnings(dispenseOrder))
            .isEmpty();
    }


}
