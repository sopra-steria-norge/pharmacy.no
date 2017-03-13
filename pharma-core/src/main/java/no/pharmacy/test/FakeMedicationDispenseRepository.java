package no.pharmacy.test;

import java.util.HashMap;
import java.util.Map;

import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationDispenseRepository;

public class FakeMedicationDispenseRepository implements MedicationDispenseRepository {

    private Map<String, DispenseOrder> collections = new HashMap<>();

    @Override
    public void saveMedicationDispenseCollection(DispenseOrder collection) {
        collections.put(collection.getIdentifier(), collection); // TODO: Clone
    }

    @Override
    public DispenseOrder getMedicationDispenseCollectionById(String id) {
        return collections.get(id);
    }
}
