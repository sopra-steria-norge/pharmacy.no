package no.pharmacy.order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.pharmacy.core.Money;
import no.pharmacy.refund.RefundGroup;

public class PurchaseOrder {

    private List<MedicationOrder> medicationOrders = new ArrayList<>();

    public void addAll(MedicationOrder... medicationOrders) {
        this.medicationOrders.addAll(Arrays.asList(medicationOrders));
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
