package no.pharmacy.order;

public interface MedicationDispenseRepository {

    void saveMedicationDispenseCollection(DispenseOrder collection);

    DispenseOrder getMedicationDispenseCollectionById(String id);

}
