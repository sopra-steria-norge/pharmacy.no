package no.pharmacy.test;

import java.util.HashMap;
import java.util.Map;

import no.pharmacy.core.PersonReference;
import no.pharmacy.patient.PersonGateway;

public class MockPersonGateway implements PersonGateway {

    private Map<String, PersonReference> names = new HashMap<>();

    private final PharmaTestData testData = new PharmaTestData();

    @Override
    public PersonReference nameByNationalId(String nationalId) {
        return names.computeIfAbsent(nationalId, n -> testData.samplePatient());
    }

    public void putPerson(String nationalId, PersonReference patientName) {
        names.put(nationalId, patientName);
    }

}
