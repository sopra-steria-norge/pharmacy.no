package no.pharmacy.dispense;

import java.util.List;

import no.pharmacy.core.PersonReference;

public interface MedicationDispenseRepository {

    void saveDispenseOrder(DispenseOrder dispenseOrder);

    DispenseOrder getDispenseOrderById(String id);

    void update(MedicationDispense medicationDispense);

    List<DispenseOrder> listReadyForPharmacist();

    void update(DispenseOrder order);

    List<DispenseOrder> historicalDispensesForPerson(PersonReference patient);

}
