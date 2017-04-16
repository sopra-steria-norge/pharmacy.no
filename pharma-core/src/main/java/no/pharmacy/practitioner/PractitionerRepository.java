package no.pharmacy.practitioner;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import no.pharmacy.core.PersonReference;

public interface PractitionerRepository {

    List<PersonReference> listDoctors();

    void refresh(URL url) throws IOException;

    Optional<Practitioner> getPractitioner(String hprNumber);

}
