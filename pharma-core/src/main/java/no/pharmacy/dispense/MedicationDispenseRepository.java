package no.pharmacy.dispense;

import java.util.List;

public interface MedicationDispenseRepository {

    void saveDispenseOrder(DispenseOrder dispenseOrder);

    DispenseOrder getDispenseOrderById(String id);

    void update(MedicationDispense medicationDispense);

    List<DispenseOrder> listReadyForPharmacist();

    void update(DispenseOrder order);

}
