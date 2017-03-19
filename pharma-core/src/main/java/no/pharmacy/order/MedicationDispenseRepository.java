package no.pharmacy.order;

import no.pharmacy.dispense.MedicationDispense;

public interface MedicationDispenseRepository {

    void saveDispenseOrder(DispenseOrder collection);

    DispenseOrder getDispenseOrderById(String id);

    void update(MedicationDispense medicationDispense);

}
