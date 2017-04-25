package no.pharmacy.patient;

import java.util.List;

import no.pharmacy.core.PersonReference;

public interface PatientRepository {

    PersonReference findPatient(String patientId);

    PersonReference findPatientByNationalId(String nationalId);

    String lookupPatientNationalId(PersonReference patient);

    List<PersonReference> queryPatient(PersonQuery personQuery);

}
