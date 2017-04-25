package no.pharmacy.patient;

import java.util.List;
import java.util.UUID;

import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.MedicationDispenseRepository;

public class HealthRecordService {

    private MedicationDispenseRepository medicationDispenseRepository;
    private PatientRepository patientRepository;

    public HealthRecordService(MedicationDispenseRepository medicationDispenseRepository, PatientRepository patientRepository) {
        this.medicationDispenseRepository = medicationDispenseRepository;
        this.patientRepository = patientRepository;
    }

    public UUID queryHealthRecord(HealthRecordQuery query) {
        return medicationDispenseRepository.saveHealthRecordQuery(query);
    }

    public List<DispenseOrder> listMedicationDispenses(UUID queryId) {
        HealthRecordQuery query = medicationDispenseRepository.retrieveHealthRecordQuery(queryId);
        return medicationDispenseRepository.listDispensesForPatient(query.getPatientId());
    }

    public HealthRecordQuery retrieveQuery(UUID queryId) {
        return medicationDispenseRepository.retrieveHealthRecordQuery(queryId);
    }

}
