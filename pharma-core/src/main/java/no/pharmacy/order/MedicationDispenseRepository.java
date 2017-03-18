package no.pharmacy.order;

public interface MedicationDispenseRepository {

    void saveDispenseOrder(DispenseOrder collection);

    DispenseOrder getMedicationDispenseCollectionById(String id);

}
