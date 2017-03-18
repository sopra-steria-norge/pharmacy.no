package no.pharmacy.order;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.dispense.MedicationDispense;

@ToString(of = { "identifier" })
public class DispenseOrder {

    @Getter @Setter
    private String identifier;

    @Getter
    private List<MedicationOrder> medicationOrders = new ArrayList<>();

    @Getter
    private List<MedicationDispense> medicationDispenses = new ArrayList<>();

    public void addMedicationOrder(MedicationOrder medicationOrder) {
        this.medicationOrders.add(medicationOrder);
        this.medicationDispenses.add(new MedicationDispense(medicationOrder));
    }

    public List<MedicationDispense> getMedicationDispenseList() {
        return medicationDispenses;
    }
}
