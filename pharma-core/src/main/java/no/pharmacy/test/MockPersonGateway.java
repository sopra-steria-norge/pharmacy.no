package no.pharmacy.test;

import java.util.HashMap;
import java.util.Map;

import no.pharmacy.patient.PersonGateway;

public class MockPersonGateway implements PersonGateway {

    private Map<String, String> names = new HashMap<>();

    @Override
    public String nameByNationalId(String nationalId) {
        return names.computeIfAbsent(nationalId, n -> PharmaTestData.sampleName());
    }

    public void putPerson(String nationalId, String patientName) {
        names.put(nationalId, patientName);
    }

}
