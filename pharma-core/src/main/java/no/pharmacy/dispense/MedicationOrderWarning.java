package no.pharmacy.dispense;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.pharmacy.medication.MedicationInteraction;

@EqualsAndHashCode
@ToString
public class MedicationOrderWarning {

    @Getter
    private final MedicationInteraction interaction;

    private final String interactingDispenseDisplay;

    @Getter
    private final Long interactingDispenseId;

    MedicationOrderWarning(MedicationDispense interactingDispense, MedicationInteraction interaction) {
        this.interaction = interaction;
        this.interactingDispenseId = interactingDispense.getId();
        this.interactingDispenseDisplay =
                interactingDispense.getMedication().getDisplay() + " forskrevet av " +
                interactingDispense.getAuthorizingPrescription().getPrescriber().getDisplay()
                + " den " + interactingDispense.getAuthorizingPrescription().getDateWritten();
    }

    MedicationOrderWarning(Long interactingDispenseId, String interactionDispenseDisplay, MedicationInteraction interaction) {
        this.interactingDispenseId = interactingDispenseId;
        this.interaction = interaction;
        this.interactingDispenseDisplay = interactionDispenseDisplay;
    }

    public String displayInteractingDispense() {
        return interactingDispenseDisplay;
    }

    public String getCode() {
        return interaction.getId();
    }
}
