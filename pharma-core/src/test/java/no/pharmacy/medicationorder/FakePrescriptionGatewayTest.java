package no.pharmacy.medicationorder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.test.FakePrescriptionGateway;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;

public class FakePrescriptionGatewayTest {

    private PharmaTestData testData = new PharmaTestData();

    private FakeReseptFormidler fakeReseptFormidler = new FakeReseptFormidler(testData.getMedicationRepository());

    private PrescriptionGateway gateway = new FakePrescriptionGateway(fakeReseptFormidler);

    @Test
    public void shouldRetrievePrescriptionList() throws Exception {
        String nationalId = testData.unusedNationalId();
        String employeeId = testData.samplePractitioner().getReference().getReference();
        MedicationOrder medicationOrder = fakeReseptFormidler.addPrescription(nationalId, testData.sampleMedication());

        List<MedicationOrderSummary> orders = gateway.requestMedicationOrdersToDispense(null, nationalId, employeeId);
        assertThat(orders)
            .extracting(o -> o.getMedicationName())
            .contains(medicationOrder.getMedication().getDisplay());

        assertThat(orders.get(0)).hasNoNullFieldsOrProperties();
    }

}
