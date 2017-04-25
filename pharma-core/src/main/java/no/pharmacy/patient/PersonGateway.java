package no.pharmacy.patient;

import no.pharmacy.core.PersonReference;

public interface PersonGateway {

    PersonReference nameByNationalId(String nationalId);

}
