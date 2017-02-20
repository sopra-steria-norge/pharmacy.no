package no.pharmacy.medication;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.Money;

@ToString
public class Medication {

    @Getter @Setter
    private Money trinnPrice;
    @Getter @Setter
    private Money retailPrice;

    public Money getUncoveredAmount() {
        return retailPrice.isGreaterThan(trinnPrice) ? retailPrice.minus(trinnPrice) : Money.zero();
    }

    public Money getCoveredAmount() {
        return retailPrice.isGreaterThan(trinnPrice) ? trinnPrice : retailPrice;
    }

}
