package no.pharmacy.order;

import java.util.List;

import no.pharmacy.dispense.MedicationDispense;

public interface MedicationDispenseRepository {

    void saveDispenseOrder(DispenseOrder collection);

    DispenseOrder getDispenseOrderById(String id);

    void update(MedicationDispense medicationDispense);

    List<DispenseOrder> listReadyForPharmacist();

}
