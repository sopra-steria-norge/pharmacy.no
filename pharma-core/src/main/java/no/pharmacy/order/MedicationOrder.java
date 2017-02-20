package no.pharmacy.order;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.Practitioner;
import no.pharmacy.medication.Medication;

@ToString
public class MedicationOrder {

    private Practitioner prescriber;
    private LocalDate dateWritten;

    @Getter @Setter
    private Medication medication;

    public void setPrescriber(Practitioner prescriber) {
        this.prescriber = prescriber;
    }

    public void setDateWritten(LocalDate dateWritten) {
        this.dateWritten = dateWritten;
    }

    public String getRefundGroup() {
        return dateWritten.toString() + "/" + prescriber.getIdentifier();
    }

}
