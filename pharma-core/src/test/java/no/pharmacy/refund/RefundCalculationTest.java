package no.pharmacy.refund;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Test;

import no.pharmacy.core.Money;
import no.pharmacy.core.Practitioner;
import no.pharmacy.medication.Medication;
import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationOrder;
import no.pharmacy.test.PharmaTestData;

public class RefundCalculationTest {

    private static final Money MAX_COPAY_PER_PRESCRIPTION = Money.inCents(52000);

    @Test
    public void onlyPriceUpToTrinnPriceIsCovered() {
        Medication medication1 = new Medication();
        medication1.setTrinnPrice(Money.inCents(10000));
        medication1.setRetailPrice(medication1.getTrinnPrice().plusCents(2000));

        assertThat(medication1.getUncoveredAmount())
            .isEqualTo(medication1.getRetailPrice().minus(medication1.getTrinnPrice()));
    }

    @Test
    public void medicationsCostingLessThanTrinnPriceAreFullyCovered() {
        Medication medication = new Medication();
        medication.setTrinnPrice(Money.inCents(20000));
        medication.setRetailPrice(medication.getTrinnPrice().plusCents(-3000));
        assertThat(medication.getUncoveredAmount())
            .isEqualTo(Money.zero());
    }

    @Test
    public void shouldCalculateCompleteRefund() throws Exception {
        DispenseOrder order = new DispenseOrder();

        Practitioner doctor1 = PharmaTestData.sampleDoctor();
        Practitioner doctor2 = PharmaTestData.sampleDoctor();

        LocalDate firstDate = LocalDate.now().minusDays(100);
        LocalDate secondDate = firstDate.plusDays(7);

        Medication medication1 = new Medication();
        medication1.setTrinnPrice(Money.inCents(10000));
        medication1.setRetailPrice(medication1.getTrinnPrice().plusCents(2000));

        Medication medication2 = new Medication();
        medication2.setTrinnPrice(Money.inCents(20000));
        medication2.setRetailPrice(medication2.getTrinnPrice().plusCents(-3000));

        MedicationOrder order1 = PharmaTestData.sampleMedicationOrder(doctor1, firstDate, medication1);
        MedicationOrder order2 = PharmaTestData.sampleMedicationOrder(doctor1, firstDate, medication2);
        MedicationOrder order3 = PharmaTestData.sampleMedicationOrder(doctor1, secondDate, medication1);
        MedicationOrder order4 = PharmaTestData.sampleMedicationOrder(doctor2, firstDate, medication1);

        order.addMedicationOrder(order1);
        order.addMedicationOrder(order2);
        order.addMedicationOrder(order3);
        order.addMedicationOrder(order4);

        assertThat(order.getUncoveredTotal()).isEqualTo(medication1.getUncoveredAmount().times(3));
        assertThat(order.getCoveredTotal())
            .isEqualTo(medication1.getCoveredAmount().times(3).plus(medication2.getCoveredAmount()));
        assertThat(order.getPatientTotal())
            .isEqualTo(medication1.getCoveredAmount().times(2).percent(39).plus(MAX_COPAY_PER_PRESCRIPTION));
    }

    @Test
    public void shouldCalculateRefundGroup() throws Exception {
        DispenseOrder order = new DispenseOrder();

        Practitioner doctor1 = PharmaTestData.sampleDoctor();

        LocalDate firstDate = LocalDate.now().minusDays(100);

        Medication medication1 = new Medication();
        medication1.setTrinnPrice(Money.inCents(100000));
        medication1.setRetailPrice(medication1.getTrinnPrice().plusCents(-2000));

        Medication medication2 = new Medication();
        medication2.setTrinnPrice(Money.inCents(20000));
        medication2.setRetailPrice(medication2.getTrinnPrice().plusCents(-3000));

        MedicationOrder order1 = PharmaTestData.sampleMedicationOrder(doctor1, firstDate, medication1);
        MedicationOrder order2 = PharmaTestData.sampleMedicationOrder(doctor1, firstDate, medication2);

        order.addMedicationOrder(order1);
        order.addMedicationOrder(order2);

        assertThat(order.getRefundGroups()).extracting(g -> g.getMedicationOrders())
            .containsOnly(Arrays.asList(order1, order2));

        assertThat(order.getRefundGroups().iterator().next().getPatientAmount())
            .isEqualTo(MAX_COPAY_PER_PRESCRIPTION);
        assertThat(order.getPatientTotal()).isEqualTo(MAX_COPAY_PER_PRESCRIPTION);
        assertThat(order.getUncoveredTotal()).isEqualTo(Money.zero());
        assertThat(order.getRefundTotal())
            .isEqualTo(medication1.getRetailPrice().plus(medication2.getRetailPrice()).minus(MAX_COPAY_PER_PRESCRIPTION));
    }

    @Test
    public void shouldCalculateRefundGroup2() throws Exception {
        DispenseOrder order = new DispenseOrder();

        Practitioner doctor1 = PharmaTestData.sampleDoctor();

        LocalDate firstDate = LocalDate.now().minusDays(100);

        Medication medication1 = new Medication();
        medication1.setTrinnPrice(Money.inCents(10000));
        medication1.setRetailPrice(medication1.getTrinnPrice().plusCents(-2000));

        order.addMedicationOrder(PharmaTestData.sampleMedicationOrder(doctor1, firstDate, medication1));

        assertThat(order.getRefundGroups().iterator().next().getPatientAmount())
            .isEqualTo(medication1.getRetailPrice().percent(39));
    }

}
