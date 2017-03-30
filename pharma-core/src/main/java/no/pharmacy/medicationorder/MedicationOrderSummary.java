package no.pharmacy.medicationorder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import no.pharmacy.dispense.MedicationOrderInfo;

@EqualsAndHashCode
public class MedicationOrderSummary implements MedicationOrderInfo {

    @Getter @Setter
    private String prescriptionId;

    @Getter @Setter
    private String medicationName;

    @Getter @Setter
    private String prescriberName;

}
