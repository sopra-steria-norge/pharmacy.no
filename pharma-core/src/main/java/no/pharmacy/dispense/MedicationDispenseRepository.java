package no.pharmacy.dispense;

import java.util.List;

public interface MedicationDispenseRepository {

    void saveDispenseOrder(DispenseOrder collection);

    DispenseOrder getDispenseOrderById(String id);

    void update(MedicationDispense medicationDispense);

    List<DispenseOrder> listReadyForPharmacist();

}
