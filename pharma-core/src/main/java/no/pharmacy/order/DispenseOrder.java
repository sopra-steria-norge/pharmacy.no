package no.pharmacy.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.Money;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationHistory;
import no.pharmacy.refund.RefundGroup;

@ToString(of = { "identifier" })
public class DispenseOrder implements MedicationHistory {

    @Getter @Setter
    private String identifier;

    @Getter
    protected List<MedicationOrder> medicationOrders = new ArrayList<>();

    @Getter
    private List<MedicationDispense> medicationDispenses = new ArrayList<>();

    @Override
    public List<MedicationDispense> getDispenses() {
        return medicationDispenses;
    }

    public MedicationDispense addMedicationOrder(MedicationOrder medicationOrder) {
        MedicationDispense dispense = new MedicationDispense(medicationOrder);
        this.medicationOrders.add(medicationOrder);
        this.medicationDispenses.add(dispense);
        return dispense;
    }

    public List<MedicationDispense> getMedicationDispenseList() {
        return medicationDispenses;
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
            result = result.plus(dispense.getUncoveredAmount());
        }
        return result;
    }

    public Money getCoveredTotal() {
        Money result = Money.zero();
        for (MedicationDispense dispense : medicationDispenses) {
            if (dispense.getPrice() == null) {
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

}
