package no.pharmacy.dispense;

import java.time.LocalDate;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.PersonReference;
import no.pharmacy.medication.Medication;
import no.pharmacy.medicationorder.MedicationOrderSummary;

@EqualsAndHashCode(of={"id", "prescriber", "dateWritten", "medication"}, callSuper=true)
@ToString
public class MedicationOrder extends MedicationOrderSummary {

    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String prescriptionId;

    @Getter @Setter
    private PersonReference prescriber;

    @Getter @Setter
    private LocalDate dateWritten;

    @Getter
    private Medication medication;

    @Getter @Setter
    private List<Medication> alternatives;

    @Getter @Setter
    private String dosageText;

    public MedicationOrder() {
        // TODO Auto-generated constructor stub
    }

    public MedicationOrder(Medication product) {
        this.medication = product;
        setMedicationName(product.getDisplay());
    }

    public String getRefundGroup() {
        return dateWritten + "/" + prescriber.getReference();
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
        setMedicationName(medication.getDisplay());
    }
}
