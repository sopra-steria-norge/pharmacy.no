package no.pharmacy.dispense;

import java.util.List;

import no.pharmacy.core.Reference;

public interface MedicationDispenseRepository {

    void saveDispenseOrder(DispenseOrder dispenseOrder);

    DispenseOrder getDispenseOrderById(String id);

    void update(MedicationDispense medicationDispense);

    List<DispenseOrder> listReadyForPharmacist();

    void update(DispenseOrder order);

    List<DispenseOrder> historicalDispensesForPerson(Reference patient);

}
