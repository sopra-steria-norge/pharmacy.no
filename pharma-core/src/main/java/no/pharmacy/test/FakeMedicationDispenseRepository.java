package no.pharmacy.test;

import java.util.HashMap;
import java.util.Map;

import no.pharmacy.order.MedicationDispenseCollection;
import no.pharmacy.order.MedicationDispenseRepository;

public class FakeMedicationDispenseRepository implements MedicationDispenseRepository {

    private Map<String, MedicationDispenseCollection> collections = new HashMap<>();

    @Override
    public void saveMedicationDispenseCollection(MedicationDispenseCollection collection) {
        collections.put(collection.getIdentifier(), collection); // TODO: Clone
    }

    @Override
    public MedicationDispenseCollection getMedicationDispenseCollectionById(String id) {
        return collections.get(id);
    }
}
