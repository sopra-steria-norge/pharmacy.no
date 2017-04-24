package no.pharmacy.dispense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.Money;
import no.pharmacy.core.PersonReference;

@ToString(of = { "identifier" })
public class DispenseOrder implements MedicationHistory {

    @Getter @Setter
    private String identifier;

    @Getter
    private List<MedicationOrder> medicationOrders = new ArrayList<>();

    @Getter
    private List<MedicationDispense> medicationDispenses = new ArrayList<>();

    @Getter @Setter
    private PersonReference patient;

    @Getter @Setter
    private String customerSignature;

    @Getter
    private boolean dispensed;

    @Override
    public List<MedicationDispense> getDispenses() {
        // TODO: Inline
        return getMedicationDispenses();
    }

    public MedicationDispense addMedicationOrder(MedicationOrder medicationOrder) {
        MedicationDispense dispense = new MedicationDispense(medicationOrder);
        this.medicationOrders.add(medicationOrder);
        this.medicationDispenses.add(dispense);
        return dispense;
    }

    public Collection<RefundGroup> getRefundGroups() {
        Map<String, RefundGroup> result = new HashMap<>();

        for (MedicationDispense dispense : medicationDispenses) {
            result.computeIfAbsent(dispense.getRefundGroup(), s -> new RefundGroup())
                .add(dispense);
        }

        return result.values();
    }

    public Money getUncoveredTotal() {
        Money result = Money.zero();
        for (MedicationDispense dispense : medicationDispenses) {
            if (dispense.getUncoveredAmount() == null) {
                return null;
            }
            result = result.plus(dispense.getUncoveredAmount());
        }
        return result;
    }

    public Money getCoveredTotal() {
        Money result = Money.zero();
        for (MedicationDispense dispense : medicationDispenses) {
            if (dispense.getCoveredAmount() == null) {
                return null;
            }
            result = result.plus(dispense.getCoveredAmount());
        }
        return result;
    }

    public Money getPatientTotal() {
        Money result = Money.zero();
        for (RefundGroup refundGroup : getRefundGroups()) {
            Money patientAmount = refundGroup.getPatientAmount();
            if (patientAmount == null) {
                return null;
            }
            result = result.plus(patientAmount);
        }
        return result;
    }

    public Money getRefundTotal() {
        Money coveredTotal = getCoveredTotal();
        return coveredTotal != null ? coveredTotal.minus(getPatientTotal()) : null;
    }

    public void createWarnings() {
        for (MedicationDispense dispense : medicationDispenses) {
            dispense.createWarnings(this);
        }
    }

    public boolean isPharmacistControlComplete() {
        for (MedicationDispense dispense : medicationDispenses) {
            if (!dispense.isPharmacistControlComplete()) {
                return false;
            }
        }
        return true;
    }

    public boolean isPackagingControlComplete() {
        for (MedicationDispense dispense : medicationDispenses) {
            if (!dispense.isPackagingControlled()) {
                return false;
            }
        }
        return true;
    }

    public boolean isSelectionComplete() {
        for (MedicationDispense dispense : medicationDispenses) {
            if (dispense.getMedication() == null || dispense.getPrice() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isReadyToDispense() {
        return !dispensed && isSelectionComplete() && isPharmacistControlComplete() && isPackagingControlComplete();
    }

    public void setDispensed() {
        this.dispensed = true;

        for (MedicationDispense medicationDispense : medicationDispenses) {
            medicationDispense.setDispensed();
        }
    }

    void setDispensed(boolean dispensed) {
        this.dispensed = dispensed;
    }

}
