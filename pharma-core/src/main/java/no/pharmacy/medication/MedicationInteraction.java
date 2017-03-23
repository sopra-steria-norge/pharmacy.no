package no.pharmacy.medication;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(of="id")
@ToString
public class MedicationInteraction {

    @Getter @Setter
    private String id;

    @Getter @Setter
    private MedicalInteractionSeverity severity;

    @Getter @Setter
    private String clinicalConsequence, interactionMechanism;

    @Getter
    private List<String> substanceCodes = new ArrayList<>();

    public boolean interactsWith(String substance) {
        for (String candidateSubstances : substanceCodes) {
            if (substance.startsWith(candidateSubstances)) {
                return true;
            }
        }
        return false;
    }

}
