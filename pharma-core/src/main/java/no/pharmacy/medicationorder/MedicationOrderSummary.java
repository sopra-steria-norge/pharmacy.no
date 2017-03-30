package no.pharmacy.medicationorder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import no.pharmacy.core.Reference;

@EqualsAndHashCode
public class MedicationOrderSummary {

    @Getter @Setter
    private String prescriptionId;

    @Getter @Setter
    private String medicationName;

    @Getter @Setter
    private Reference subject;


}
