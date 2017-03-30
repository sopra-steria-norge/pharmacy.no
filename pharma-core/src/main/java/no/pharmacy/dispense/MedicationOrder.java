package no.pharmacy.dispense;

import java.time.LocalDate;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.PersonReference;
import no.pharmacy.core.Reference;
import no.pharmacy.medication.Medication;

@EqualsAndHashCode(of={"id", "prescriber", "dateWritten", "medication"})
@ToString
public class MedicationOrder implements MedicationOrderInfo {

    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String prescriptionId;

    @Getter @Setter
    private PersonReference prescriber;

    @Getter @Setter
    private LocalDate dateWritten;

    @Getter @Setter
    private Medication medication;

    @Getter @Setter
    private Reference subject;

    @Getter @Setter
    private List<Medication> alternatives;

    @Getter @Setter
    private String dosageText;

    public MedicationOrder() {
        // TODO Auto-generated constructor stub
    }

    public MedicationOrder(Medication product) {
        this.medication = product;
    }

    public String getRefundGroup() {
        return dateWritten + "/" + prescriber.getReference();
    }

    public String getMedicationName() {
        return medication.getDisplay();
    }
}
