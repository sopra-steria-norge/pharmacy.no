package no.pharmacy.medicationorder;

import java.time.LocalDate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.dispense.MedicationOrderInfo;

@EqualsAndHashCode
@ToString
public class MedicationOrderSummary implements MedicationOrderInfo {

    @Getter @Setter
    private String prescriptionId;

    @Getter @Setter
    private String medicationName;

    @Getter @Setter
    private String prescriberName;

    @Getter @Setter
    private LocalDate dateWritten;
}
