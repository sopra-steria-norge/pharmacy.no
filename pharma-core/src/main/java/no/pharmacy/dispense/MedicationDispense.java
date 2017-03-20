package no.pharmacy.dispense;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import no.pharmacy.core.Money;
import no.pharmacy.medication.Medication;
import no.pharmacy.order.MedicationOrder;

@EqualsAndHashCode(of={"id", "medication", "authorizingPrescription" })
public class MedicationDispense {

    @Getter @Setter
    private Long id;

    public MedicationDispense(MedicationOrder authorizingPrescription) {
        this.authorizingPrescription = authorizingPrescription;
    }

    public MedicationDispense() {
    }

    @Getter @Setter
    private Medication medication;

    @Getter @Setter
    private Money price;

    @Getter @Setter
    private MedicationOrder authorizingPrescription;

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "(medication=" + medication + ",price=" + price
                + ",authorizingPrescription=" + getPrescriptionId();
    }

    private String getPrescriptionId() {
        return authorizingPrescription != null
                ? "prescription-" + authorizingPrescription.getId()
                : null;
    }

    public String getRefundGroup() {
        return authorizingPrescription.getRefundGroup();
    }

    public Money getCoveredAmount() {
        return getMedication().getCoveredAmount(getPrice());
    }

    public Money getUncoveredAmount() {
        return getMedication().getUncoveredAmount(getPrice());
    }

}
