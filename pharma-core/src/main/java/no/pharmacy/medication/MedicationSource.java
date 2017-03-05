package no.pharmacy.medication;

import java.util.Optional;

public interface MedicationSource {

    Optional<Medication> getMedication(String productId);

}
