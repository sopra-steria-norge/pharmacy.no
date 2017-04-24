package no.pharmacy.dispense;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationInteraction;
import no.pharmacy.test.PharmaTestData;

public class DispenseOrderTest {

    private PharmaTestData testData = new PharmaTestData();

    @Test
    public void shouldListDrugInteractions() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        Medication ritalin = testData.getMedication("500595");
        Medication aurorix = testData.getMedication("466813");

        assertThat(ritalin.getInteractionsWith(aurorix))
            .extracting(MedicationInteraction::getId)
            .contains("ID_06688DFC-BF07-4113-A6E4-9F8F00E5A536");

        MedicationDispense ritalinDispense = dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(ritalin));
        ritalinDispense.setMedication(ritalin);
        dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(aurorix)).setMedication(aurorix);
        dispenseOrder.createWarnings();

        Assertions.assertThat(ritalinDispense.getWarningActions())
            .extracting(a -> a.getId())
            .contains("ID_06688DFC-BF07-4113-A6E4-9F8F00E5A536");
    }

    @Test
    public void shouldNotGiveInteractionsOnSimpleOrder() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        Medication ritalin = testData.getMedication("500595");
        MedicationDispense ritalinDispense = dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(ritalin));
        ritalinDispense.setMedication(ritalin);
        dispenseOrder.createWarnings();

        Assertions.assertThat(ritalinDispense.getWarningActions())
            .isEmpty();
    }


}
