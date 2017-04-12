package no.pharmacy.medication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.Money;

@EqualsAndHashCode(of={"productId", "display"})
@ToString(exclude="xml")
@NoArgsConstructor
public class Medication {

    @Getter @Setter
    private Money trinnPrice;

    @Getter @Setter
    private String productId;

    @Getter @Setter
    private String gtin;

    @Getter @Setter
    private String display;

    @Getter @Setter
    /** ATC code */
    private String substance;

    @Getter @Setter
    private String substitutionGroup;

    @Getter @Setter
    private String xml;

    @Getter
    private List<MedicationInteraction> interactions = new ArrayList<>();

    public Medication(String productId, String name, int trinnPriceInCents) {
        this.productId = productId;
        this.display = name;
        this.trinnPrice = Money.inCents(trinnPriceInCents);
    }

    public Money getUncoveredAmount(Money retailPrice) {
        return trinnPrice != null && retailPrice.isGreaterThan(trinnPrice) ? retailPrice.minus(trinnPrice) : Money.zero();
    }

    public Money getCoveredAmount(Money retailPrice) {
        return trinnPrice != null && retailPrice.isGreaterThan(trinnPrice) ? trinnPrice : retailPrice;
    }

    public List<MedicationInteraction> getInteractionsWith(Medication otherMedication) {
        if (otherMedication == null) {
            return new ArrayList<>();
        }
        return getInteractions().stream()
            .filter(i -> i.interactsWith(otherMedication.getSubstance()))
            .collect(Collectors.toList());
    }



}
