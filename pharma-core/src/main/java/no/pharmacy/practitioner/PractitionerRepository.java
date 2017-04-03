package no.pharmacy.practitioner;

import java.util.List;

import no.pharmacy.core.PersonReference;

public interface PractitionerRepository {

    List<PersonReference> listDoctors();

    void refresh(String hprLocation);

}
