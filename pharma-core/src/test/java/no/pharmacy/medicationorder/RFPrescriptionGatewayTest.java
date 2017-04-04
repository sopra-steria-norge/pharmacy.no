package no.pharmacy.medicationorder;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import org.junit.Test;

import no.pharmacy.core.PersonReference;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.patient.JdbcPatientRepository;
import no.pharmacy.patient.PatientRepository;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class RFPrescriptionGatewayTest {

    private PharmaTestData testData = new PharmaTestData();

    private PatientRepository patientRepository = new JdbcPatientRepository(TestDataSource.patientInstance(), s -> PharmaTestData.sampleName(), CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));

    private FakeReseptFormidler fakeReseptFormidler = new FakeReseptFormidler(testData.getMedicationRepository(), patientRepository);

    private PrescriptionGateway gateway = new RFPrescriptionGateway(fakeReseptFormidler, testData.getMedicationRepository());

    private PersonReference prescriber = testData.sampleDoctor();

    private String employeeId = testData.samplePractitioner().getReference().getReference();

    @Test
    public void shouldRetrievePrescriptionList() {
        String nationalId = testData.unusedNationalId();
        MedicationOrder medicationOrder = fakeReseptFormidler.addPrescription(nationalId, testData.sampleMedication(), prescriber);

        List<MedicationOrderSummary> orders = gateway.requestMedicationOrdersToDispense(null, nationalId, employeeId);
        assertThat(orders)
            .extracting(o -> o.getMedicationName())
            .contains(medicationOrder.getMedication().getDisplay());

        assertThat(orders.get(0))
            .hasNoNullFieldsOrPropertiesExcept("subject");
    }

    @Test
    public void shouldStartPrescriptionDispense() {
        MedicationOrder medicationOrder = fakeReseptFormidler.addPrescription(testData.unusedNationalId(), testData.sampleMedication(), prescriber);

        MedicationOrder orderForDispense = gateway.startMedicationOrderDispense(medicationOrder.getPrescriptionId(), null, employeeId);

        assertThat(orderForDispense)
            .isEqualToIgnoringGivenFields(medicationOrder, "subject");
    }

    @Test
    public void shouldCompleteDispense() {
        MedicationOrder medicationOrder = fakeReseptFormidler.addPrescription(testData.unusedNationalId(), testData.sampleMedication(), prescriber);

        MedicationDispense dispense = new MedicationDispense(medicationOrder);
        dispense.setMedication(dispense.getAuthorizingPrescription().getMedication());
        dispense.setPrice(PharmaTestData.samplePrice());
        dispense.setPrintedDosageText("Updated dosage text");
        dispense.setConfirmedByPharmacist(true);
        dispense.setPackagingControlled(true);
        dispense.setDispensed();

        gateway.completeDispense(dispense, employeeId);

        assertThat(fakeReseptFormidler.getPrintedDosageTexts(medicationOrder))
            .contains(dispense.getPrintedDosageText());
    }

}
