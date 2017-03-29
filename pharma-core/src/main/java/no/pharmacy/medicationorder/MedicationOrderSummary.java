package no.pharmacy.medicationorder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class MedicationOrderSummary {

    @Getter @Setter
    private String prescriptionId;

    @Getter @Setter
    private String medicationName;


}
