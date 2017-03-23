package no.pharmacy.dispense;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import no.pharmacy.core.Money;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationInteraction;
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

    public List<MedicationOrderWarning> getWarnings(MedicationHistory history) {
        ArrayList<MedicationOrderWarning> result = new ArrayList<>();

        for (MedicationDispense historicalDispense : history.getDispenses()) {
            if (historicalDispense.equals(this)) continue;
            for (MedicationInteraction interaction : getInteractions(historicalDispense)) {
                result.add(new MedicationOrderWarning(historicalDispense, interaction));
            }
        }

        return result;
    }

    private List<MedicationInteraction> getInteractions(MedicationDispense historicalDispense) {
        return medication.getInteractions().stream()
            .filter(i -> i.interactsWith(historicalDispense.getMedication().getSubstance()))
            .collect(Collectors.toList());
    }

}
