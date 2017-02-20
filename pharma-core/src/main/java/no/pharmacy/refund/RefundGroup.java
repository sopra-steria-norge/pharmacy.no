package no.pharmacy.refund;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import no.pharmacy.core.Money;
import no.pharmacy.order.MedicationOrder;

@ToString
public class RefundGroup {

    private static final Money MAX_PER_PRESCRIPTION = Money.inCents(52000);
    @Getter
    private List<MedicationOrder> medicationOrders = new ArrayList<>();

    public void add(MedicationOrder medicationOrder) {
        medicationOrders.add(medicationOrder);
    }

    public Money getPatientAmount() {
        Money coveredAmount = getCoveredSum().percent(39);
        return coveredAmount.isGreaterThan(MAX_PER_PRESCRIPTION) ? MAX_PER_PRESCRIPTION : coveredAmount;
    }

    private Money getCoveredSum() {
        Money result = Money.zero();
        for (MedicationOrder medicationOrder : medicationOrders) {
            result = result.plus(medicationOrder.getMedication().getCoveredAmount());
        }
        return result;
    }

}
