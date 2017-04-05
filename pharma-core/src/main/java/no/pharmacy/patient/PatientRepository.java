package no.pharmacy.patient;

import no.pharmacy.core.PersonReference;

public interface PatientRepository {

    PersonReference findPatient(String patientId);

    PersonReference findPatientByNationalId(String nationalId);

    String lookupPatientNationalId(PersonReference patient);

}
