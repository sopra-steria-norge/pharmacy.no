package no.pharmacy.medicationorder;

import java.util.List;

import no.pharmacy.dispense.MedicationOrder;

public interface PrescriptionsSource {

    // TODO: alternative versions for name+birthdate and potentially foreigners?
    List<? extends MedicationOrderSummary> prescriptionsForPerson(String nationalId);

    MedicationOrder getById(String id);

}
