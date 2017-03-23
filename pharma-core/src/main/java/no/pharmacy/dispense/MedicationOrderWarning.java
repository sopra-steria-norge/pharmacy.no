package no.pharmacy.dispense;

import lombok.Getter;
import lombok.ToString;
import no.pharmacy.medication.MedicationInteraction;

@ToString
public class MedicationOrderWarning {

    @Getter
    private final MedicationInteraction interaction;

    @Getter
    private final MedicationDispense interactingDispense;

    public MedicationOrderWarning(MedicationDispense interactingDispense, MedicationInteraction interaction) {
        this.interactingDispense = interactingDispense;
        this.interaction = interaction;
    }
}
