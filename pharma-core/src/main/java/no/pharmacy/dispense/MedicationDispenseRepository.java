package no.pharmacy.dispense;

import java.util.List;
import java.util.UUID;
import no.pharmacy.core.PersonReference;
import no.pharmacy.medicationorder.MedicationOrderSummary;
import no.pharmacy.patient.HealthRecordQuery;

public interface MedicationDispenseRepository {

    void saveDispenseOrder(DispenseOrder dispenseOrder);

    DispenseOrder getDispenseOrderById(String id);

    void update(MedicationDispense medicationDispense);

    List<DispenseOrder> listReadyForPharmacist();

    void update(DispenseOrder order);

    List<DispenseOrder> historicalDispensesForPerson(PersonReference patient);

    List<MedicationOrderSummary> listPrescriptionsFromQuery(UUID id);

    void savePrescriptionQuery(MedicationOrderQuery query);

    HealthRecordQuery retrieveHealthRecordQuery(UUID queryId);

    UUID saveHealthRecordQuery(HealthRecordQuery query);

    List<DispenseOrder> listDispensesForPatient(UUID patientId);

}
