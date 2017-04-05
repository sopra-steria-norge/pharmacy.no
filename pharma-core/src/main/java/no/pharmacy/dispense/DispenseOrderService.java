package no.pharmacy.dispense;

import java.util.List;

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

    public DispenseOrder startDispenseOrder(String nationalId, List<String> prescriptionIds) {
        DispenseOrder dispenseOrder = new DispenseOrder();
        dispenseOrder.setPatient(patientRepository.findPatientByNationalId(nationalId));

        for (String id : prescriptionIds) {
            dispenseOrder.addMedicationOrder(prescriptionsGateway.startMedicationOrderDispense(id, null, "124"));
        }

        medicationDispenseRepository.saveDispenseOrder(dispenseOrder);
        return dispenseOrder;
    }
}
