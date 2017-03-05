package no.pharmacy.order;

public interface MedicationDispenseRepository {

    void saveMedicationDispenseCollection(MedicationDispenseCollection collection);

    MedicationDispenseCollection getMedicationDispenseCollectionById(String id);

}
