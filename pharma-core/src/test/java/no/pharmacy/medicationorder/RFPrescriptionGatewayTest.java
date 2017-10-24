package no.pharmacy.medicationorder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import no.pharmacy.core.PersonReference;
import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.DispenseOrderService;
import no.pharmacy.dispense.JdbcMedicationDispenseRepository;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.infrastructure.messages.EbXmlMessage;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.organization.JdbcHealthcareServiceRepository;
import no.pharmacy.patient.JdbcPatientRepository;
import no.pharmacy.patient.PatientRepository;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class RFPrescriptionGatewayTest {

    private PharmaTestData testData = new PharmaTestData();

    private PatientRepository patientRepository = new JdbcPatientRepository(TestDataSource.patientInstance(), s -> testData.samplePatient(), CryptoUtil.aesKey("sndglsngl ndsglsn".getBytes()));

    private FakeReseptFormidler fakeReseptFormidler = new FakeReseptFormidler(testData.getMedicationRepository(), patientRepository);

    private PrescriptionGateway gateway = new RFPrescriptionGateway(fakeReseptFormidler, testData.getMedicationRepository(), patientRepository);

    private PersonReference prescriber = testData.sampleDoctor();

    private String employeeId = testData.samplePractitioner().getReference().getReference();

    private MedicationDispenseRepository medicationDispenseRepository = new JdbcMedicationDispenseRepository(TestDataSource.pharmacistInstance(),
            new JdbcMedicationRepository(TestDataSource.medicationInstance()),
            new JdbcHealthcareServiceRepository(TestDataSource.organizationsDataSource(), JdbcHealthcareServiceRepository.SEED_URL));

    private DispenseOrderService dispenseOrderService = new DispenseOrderService(gateway, medicationDispenseRepository, patientRepository);

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
        dispense.setDateDispensed(PharmaTestData.randomPastDate(14));

        gateway.completeDispense(dispense, employeeId, testData.sampleHealthcareService());

        assertThat(fakeReseptFormidler.getPrintedDosageTexts(medicationOrder))
            .contains(dispense.getPrintedDosageText());
    }

    @Test
    public void shouldStartDispenseOrder() {
        String nationalId = testData.unusedNationalId();
        MedicationOrder medicationOrder1 = fakeReseptFormidler.addPrescription(nationalId, testData.sampleMedication(), prescriber);
        MedicationOrder medicationOrder2 = fakeReseptFormidler.addPrescription(nationalId, testData.sampleMedication(), prescriber);

        DispenseOrder dispenseOrder = dispenseOrderService.startDispenseOrder(
                Arrays.asList(medicationOrder1.getPrescriptionId(), medicationOrder2.getPrescriptionId()));

        assertThat(dispenseOrder.getDispenses())
            .extracting(MedicationDispense::getAuthorizingPrescription)
            .extracting(MedicationOrder::getPrescriptionId)
            .containsOnly(medicationOrder1.getPrescriptionId(), medicationOrder2.getPrescriptionId());
    }

    @Test
    public void shouldCompleteMedicationDispense() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        dispenseOrder.setDispensingOrganization(testData.sampleHealthcareService());
        MedicationOrder medicationOrder = testData.sampleMedicationOrder();
        MedicationDispense dispense = dispenseOrder.addMedicationOrder(medicationOrder);
        dispense.setMedication(medicationOrder.getMedication());
        dispense.setPrice(PharmaTestData.samplePrice());
        dispense.setPrintedDosageText("Corrected text");

        dispenseOrderService.completeDispenseOrder(dispenseOrder);

        EbXmlMessage dispenseMessage = fakeReseptFormidler
                .getPrescriptionMessages(medicationOrder.getPrescriptionId())
                .singleDispenseMessage();
        assertThat(dispenseMessage.getContent()
                .find("Utleveringsrapport", "Utlevering", "ReseptId").first().text())
            .isEqualTo(medicationOrder.getPrescriptionId());
        dispenseMessage.verifySignature();
        assertThat(dispenseMessage.getCertificate().getSubjectDN().toString())
            .isEqualTo(dispenseOrder.getDispensingOrganization().getDN());
    }

    @Test
    public void shouldStartPrescriptionQuery() {
        String nationalId = testData.unusedNationalId();
        MedicationOrder medicationOrder1 = fakeReseptFormidler.addPrescription(nationalId, testData.sampleMedication(), prescriber);
        MedicationOrder medicationOrder2 = fakeReseptFormidler.addPrescription(nationalId, testData.sampleMedication(), prescriber);

        UUID id = dispenseOrderService.startPrescriptionQuery(null, employeeId, nationalId);

        assertThat(medicationDispenseRepository.listPrescriptionsFromQuery(id))
            .extracting(MedicationOrderSummary::getMedicationName)
            .contains(medicationOrder1.getMedicationName(), medicationOrder2.getMedicationName());
        assertThat(medicationDispenseRepository.listPrescriptionsFromQuery(id).get(0))
            .hasNoNullFieldsOrProperties();
    }

}
