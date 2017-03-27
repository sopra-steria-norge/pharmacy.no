package no.pharmacy.dispense;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import no.pharmacy.core.Money;

@ToString
public class RefundGroup {

    private static final Money MAX_PER_PRESCRIPTION = Money.inCents(52000);
    @Getter
    private List<MedicationDispense> medicationDispense = new ArrayList<>();

    public void add(MedicationDispense dispense) {
        medicationDispense.add(dispense);
    }

    public Money getPatientAmount() {
        Money coveredSum = getCoveredSum();
        if (coveredSum == null) {
            return null;
        }
        Money coveredAmount = coveredSum.percent(39);
        return coveredAmount.isGreaterThan(MAX_PER_PRESCRIPTION) ? MAX_PER_PRESCRIPTION : coveredAmount;
    }

    private Money getCoveredSum() {
        Money result = Money.zero();
        for (MedicationDispense dispense : medicationDispense) {
            if (dispense.getPrice() == null) {
                return null;
            }
            result = result.plus(dispense.getCoveredAmount());
        }
        return result;
    }

}
