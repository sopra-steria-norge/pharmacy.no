package no.pharmacy.dispense;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import no.pharmacy.medicationorder.MedicationOrderSummary;
import no.pharmacy.medicationorder.PrescriptionGateway;
import no.pharmacy.patient.PatientRepository;

public class DispenseOrderService {

    private PrescriptionGateway prescriptionsGateway;
    private MedicationDispenseRepository medicationDispenseRepository;
    private PatientRepository patientRepository;

    public DispenseOrderService(
            PrescriptionGateway prescriptionsGateway,
            MedicationDispenseRepository medicationDispenseRepository,
            PatientRepository patientRepository) {
        this.prescriptionsGateway = prescriptionsGateway;
        this.medicationDispenseRepository = medicationDispenseRepository;
        this.patientRepository = patientRepository;
    }

    public DispenseOrder startDispenseOrder(List<String> prescriptionIds) {
        DispenseOrder dispenseOrder = new DispenseOrder();

        Set<String> potentialNationalIds = new HashSet<>();

        for (String id : prescriptionIds) {
            MedicationOrder dispense = prescriptionsGateway.startMedicationOrderDispense(id, null, "124");
            potentialNationalIds.add(dispense.getSubject().getReference());
            dispenseOrder.addMedicationOrder(dispense);
        }

        if (potentialNationalIds.size() != 1) {
            throw new IllegalArgumentException("All medication orders should be for one patient, was " + potentialNationalIds.size());
        }

        dispenseOrder.setPatient(patientRepository.findPatientByNationalId(potentialNationalIds.iterator().next()));
        dispenseOrder.createWarnings();


        medicationDispenseRepository.saveDispenseOrder(dispenseOrder);
        return dispenseOrder;
    }

    public UUID startPrescriptionQuery(String hprNumber, String herNumber, String nationalId) {
        MedicationOrderQuery query = new MedicationOrderQuery(herNumber, hprNumber);
        List<MedicationOrderSummary> prescriptions = prescriptionsGateway.requestMedicationOrdersToDispense(null, nationalId, herNumber);
        query.setPrescriptions(prescriptions);

        medicationDispenseRepository.savePrescriptionQuery(query);
        return query.getId();
    }
}
