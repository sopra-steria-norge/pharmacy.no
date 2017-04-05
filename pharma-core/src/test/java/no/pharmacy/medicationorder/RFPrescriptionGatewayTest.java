package no.pharmacy.medicationorder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import no.pharmacy.core.PersonReference;
import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.DispenseOrderService;
import no.pharmacy.dispense.JdbcMedicationDispenseRepository;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.patient.JdbcPatientRepository;
import no.pharmacy.patient.PatientRepository;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class RFPrescriptionGatewayTest {

    private PharmaTestData testData = new PharmaTestData();

    private PatientRepository patientRepository = new JdbcPatientRepository(TestDataSource.patientInstance(), s -> PharmaTestData.sampleName(), CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));

    private FakeReseptFormidler fakeReseptFormidler = new FakeReseptFormidler(testData.getMedicationRepository(), patientRepository);

    private PrescriptionGateway gateway = new RFPrescriptionGateway(fakeReseptFormidler, testData.getMedicationRepository(), patientRepository);

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
            .hasNoNullFieldsOrPropertiesExcept("id", "alternatives")
            .isEqualToIgnoringGivenFields(medicationOrder);
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

    @Test
    public void shouldStartDispenseOrder() {
        MedicationDispenseRepository medicationDispenseRepository = new JdbcMedicationDispenseRepository(TestDataSource.pharmacistInstance(), new JdbcMedicationRepository(TestDataSource.medicationInstance()));
        DispenseOrderService dispenseOrderService = new DispenseOrderService(gateway, medicationDispenseRepository, patientRepository);

        String nationalId = testData.unusedNationalId();
        MedicationOrder medicationOrder1 = fakeReseptFormidler.addPrescription(nationalId, testData.sampleMedication(), prescriber);
        MedicationOrder medicationOrder2 = fakeReseptFormidler.addPrescription(nationalId, testData.sampleMedication(), prescriber);

        DispenseOrder dispenseOrder = dispenseOrderService.startDispenseOrder(nationalId,
                Arrays.asList(medicationOrder1.getPrescriptionId(), medicationOrder2.getPrescriptionId()));

        assertThat(dispenseOrder.getDispenses())
            .extracting(MedicationDispense::getAuthorizingPrescription)
            .extracting(MedicationOrder::getPrescriptionId)
            .containsOnly(medicationOrder1.getPrescriptionId(), medicationOrder2.getPrescriptionId());
    }

}
