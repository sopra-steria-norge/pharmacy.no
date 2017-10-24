package no.pharmacy.medicationorder;

import java.util.List;

import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.organization.HealthcareService;

public interface PrescriptionGateway {

    List<MedicationOrderSummary> requestMedicationOrdersToDispense(String purpose, String nationalId, String employeeId);

    List<MedicationOrderSummary> requestMedicationOrdersToDispense(String purpose, String birthDate, String firstName, String lastName, String employeeId);

    List<MedicationOrderSummary> requestMedicationOrdersToDispenseByReference(String purpose, String referenceNumber, String employeeId);


    MedicationOrder startMedicationOrderDispense(String prescriptionId, String referenceNumber, String employeeId);

    MedicationOrder cancelMedicationOrderDispense(String prescriptionId, String referenceNumber, String employeeId);


    void completeDispense(MedicationDispense dispense, String employeeId, HealthcareService dispensingOrganization);

    void cancelDispense(String reason, String referenceNumber, String employeeId);

}
