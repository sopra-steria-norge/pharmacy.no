package no.pharmacy.gui.server.prescriptions;

import java.util.List;

import no.pharmacy.order.MedicationOrder;

public interface PrescriptionsSource {

    // TODO: alternative versions for name+birthdate and potentially foreigners?
    List<MedicationOrder> prescriptionsForPerson(String nationalId);

    MedicationOrder getById(String id);

}
