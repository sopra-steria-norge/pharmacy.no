package no.pharmacy.practitioner;

import java.util.List;
import java.util.Optional;

import no.pharmacy.core.PersonReference;

public interface PractitionerRepository {

    List<PersonReference> listDoctors();

    void refresh(String hprLocation);

    Optional<Practitioner> getPractitioner(String hprNumber);

}
