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
import no.pharmacy.refund.RefundGroup;

@ToString(of = { "identifier" })
public class DispenseOrder {

    @Getter @Setter
    private String identifier;

    @Getter
    protected List<MedicationOrder> medicationOrders = new ArrayList<>();

    @Getter
    private List<MedicationDispense> medicationDispenses = new ArrayList<>();

    public void addMedicationOrder(MedicationOrder medicationOrder) {
        this.medicationOrders.add(medicationOrder);
        this.medicationDispenses.add(new MedicationDispense(medicationOrder));
    }

    public List<MedicationDispense> getMedicationDispenseList() {
        return medicationDispenses;
    }

    public Collection<RefundGroup> getRefundGroups() {
        Map<String, RefundGroup> result = new HashMap<>();

        for (MedicationOrder medicationOrder : medicationOrders) {
            result.computeIfAbsent(medicationOrder.getRefundGroup(), s -> new RefundGroup())
                .add(medicationOrder);
        }

        return result.values();
    }

    public Money getUncoveredTotal() {
        Money result = Money.zero();
        for (MedicationOrder medicationOrder : medicationOrders) {
            result = result.plus(medicationOrder.getMedication().getUncoveredAmount());
        }
        return result;
    }

    public Money getCoveredTotal() {
        Money result = Money.zero();
        for (MedicationOrder medicationOrder : medicationOrders) {
            result = result.plus(medicationOrder.getMedication().getCoveredAmount());
        }
        return result;
    }

    public Money getPatientTotal() {
        Money result = Money.zero();
        for (RefundGroup refundGroup : getRefundGroups()) {
            result = result.plus(refundGroup.getPatientAmount());
        }
        return result;
    }

    public Money getRefundTotal() {
        return getCoveredTotal().minus(getPatientTotal());
    }
}
