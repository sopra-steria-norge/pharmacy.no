package no.pharmacy.patient;

import no.pharmacy.core.Reference;

public interface PatientRepository {

    Reference findPatient(String patientId);

    Reference findPatientByNationalId(String nationalId);

}
