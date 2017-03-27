package no.pharmacy.dispense;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.Test;

import no.pharmacy.core.Money;
import no.pharmacy.core.Reference;
import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.medication.Medication;
import no.pharmacy.test.PharmaTestData;

public class RefundCalculationTest {

    private PharmaTestData testData = new PharmaTestData();

    private static final Money MAX_COPAY_PER_PRESCRIPTION = Money.inCents(52000);

    @Test
    public void onlyPriceUpToTrinnPriceIsCovered() {
        Medication medication1 = new Medication();
        medication1.setTrinnPrice(Money.inCents(10000));
        Money retailPrice = medication1.getTrinnPrice().plusCents(2000);

        assertThat(medication1.getUncoveredAmount(retailPrice))
            .isEqualTo(retailPrice.minus(medication1.getTrinnPrice()));
    }

    @Test
    public void medicationsCostingLessThanTrinnPriceAreFullyCovered() {
        Medication medication = new Medication();
        medication.setTrinnPrice(Money.inCents(20000));
        Money retailPrice = medication.getTrinnPrice().plusCents(-3000);
        assertThat(medication.getUncoveredAmount(retailPrice))
            .isEqualTo(Money.zero());
    }

    @Test
    public void shouldCalculateCompleteRefund() throws Exception {
        DispenseOrder order = new DispenseOrder();

        Reference doctor1 = testData.sampleDoctor();
        Reference doctor2 = testData.sampleDoctor();

        LocalDate firstDate = LocalDate.now().minusDays(100);
        LocalDate secondDate = firstDate.plusDays(7);

        Medication medication1 = new Medication();
        medication1.setTrinnPrice(Money.inCents(10000));
        Money retailPrice1 = medication1.getTrinnPrice().plusCents(2000);

        Medication medication2 = new Medication();
        medication2.setTrinnPrice(Money.inCents(500000));
        Money retailPrice2 = medication2.getTrinnPrice().plusCents(-3000);

        addMedicationDispense(order, doctor1, firstDate, medication1, retailPrice1);
        addMedicationDispense(order, doctor1, firstDate, medication2, retailPrice2);
        addMedicationDispense(order, doctor1, secondDate, medication1, retailPrice1);
        addMedicationDispense(order, doctor2, firstDate, medication1, retailPrice1);

        assertThat(order.getUncoveredTotal()).isEqualTo(medication1.getUncoveredAmount(retailPrice1).times(3));
        assertThat(order.getCoveredTotal())
            .isEqualTo(medication1.getCoveredAmount(retailPrice1).times(3)
                    .plus(medication2.getCoveredAmount(retailPrice2)));
        assertThat(order.getPatientTotal())
            .isEqualTo(medication1.getCoveredAmount(retailPrice1).times(2).percent(39)
                    .plus(MAX_COPAY_PER_PRESCRIPTION));
    }

    @Test
    public void shouldCalculateRefundGroup() throws Exception {
        DispenseOrder order = new DispenseOrder();

        Reference doctor1 = testData.sampleDoctor();

        LocalDate firstDate = LocalDate.now().minusDays(100);

        Medication medication1 = new Medication();
        medication1.setTrinnPrice(Money.inCents(100000));
        Money retailPrice1 = medication1.getTrinnPrice().plusCents(-2000);

        Medication medication2 = new Medication();
        medication2.setTrinnPrice(Money.inCents(50000));
        Money retailPrice2 = medication2.getTrinnPrice().plusCents(-3000);

        addMedicationDispense(order, doctor1, firstDate, medication1, retailPrice1);
        addMedicationDispense(order, doctor1, firstDate, medication2, retailPrice2);

        assertThat(order.getRefundGroups().iterator().next().getPatientAmount())
            .isEqualTo(MAX_COPAY_PER_PRESCRIPTION);
        assertThat(order.getPatientTotal()).isEqualTo(MAX_COPAY_PER_PRESCRIPTION);
        assertThat(order.getUncoveredTotal()).isEqualTo(Money.zero());
        assertThat(order.getRefundTotal())
            .isEqualTo(retailPrice1.plus(retailPrice2).minus(MAX_COPAY_PER_PRESCRIPTION));
    }

    private void addMedicationDispense(DispenseOrder order,
            Reference practitioner, LocalDate firstDate,
            Medication medication, Money retailPrice) {
        MedicationOrder prescription = PharmaTestData.sampleMedicationOrder(practitioner, firstDate, medication);
        MedicationDispense dispense = order.addMedicationOrder(prescription);
        dispense.setPrice(retailPrice);
        dispense.setMedication(medication);
    }

    @Test
    public void shouldCalculateRefundGroup2() throws Exception {
        DispenseOrder order = new DispenseOrder();

        Reference doctor1 = testData.sampleDoctor();

        LocalDate firstDate = LocalDate.now().minusDays(100);

        Medication medication1 = new Medication();
        medication1.setTrinnPrice(Money.inCents(10000));
        Money retailPrice1 = medication1.getTrinnPrice().plusCents(-2000);

        addMedicationDispense(order, doctor1, firstDate, medication1, retailPrice1);

        assertThat(order.getRefundGroups().iterator().next().getPatientAmount())
            .isEqualTo(retailPrice1.percent(39));
    }

}
