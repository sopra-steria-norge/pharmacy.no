package no.pharmacy.web.dispense;

import java.util.List;

import no.pharmacy.dispense.MedicationOrder;

public interface PrescriptionsSource {

    // TODO: alternative versions for name+birthdate and potentially foreigners?
    List<MedicationOrder> prescriptionsForPerson(String nationalId);

    MedicationOrder getById(String id);

}
