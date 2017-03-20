package no.pharmacy.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationSource;

public class FakeMedicationSource implements MedicationSource {

    private Medication[] SAMPLE_MEDICATIONS = new Medication[] {
            new Medication("163089", "Ritalin, 10 mg, 30 stk. (blister)", 10000),
            new Medication("500595", "Ritalin, 10 mg, 200 stk. (blister)", 10000),
            new Medication("077615", "Medikinet, 10 mg, 30 stk. (blister)", 10000),
            new Medication("044079", "Medikinet, 10 mg, 100 stk. (blister)", 10000),
            new Medication("466813", "Aurorix, TABLETTER, filmdrasjerte, 150 mg, 100 stk. (blister)", 10000),
            new Medication("587857", "Aurorix, TABLETTER, filmdrasjerte, 300 mg, 60 stk. (blister)", 10000),
            new Medication("168740", "Tegretol Retard, DEPOTTABLETTER, 200 mg, 200 stk, blister", 10000),
            new Medication("473900", "Tegretol Retard, DEPOTTABLETTER, 200 mg, 200 stk, boks", 10000),
            new Medication("168971", "Tegretol Retard, DEPOTTABLETTER, 400 mg, 200 stk, blister", 10000),
            new Medication("473918", "Tegretol Retard, DEPOTTABLETTER, 400 mg, 200 stk, boks", 10000),
    };

    private Map<String, Medication> medications = new HashMap<>();

    public FakeMedicationSource() {
        for (Medication medication : SAMPLE_MEDICATIONS) {
            this.medications.put(String.valueOf(medication.getProductId()), medication);
        }
    }


    @Override
    public Optional<Medication> getMedication(String productId) {
        return Optional.ofNullable(medications.get(productId));
    }

    public Medication pickOne() {
        List<Medication> values = new ArrayList<>(medications.values());
        return values.get(new Random().nextInt(values.size()));
    }

}
