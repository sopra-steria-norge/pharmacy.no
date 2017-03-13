package no.pharmacy.order;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class DispenseOrder {

    @Getter @Setter
    private String identifier;

    @Getter
    private List<MedicationOrder> medicationOrders = new ArrayList<>();

    public void addMedicationOrder(MedicationOrder medicationOrder) {
        this.medicationOrders.add(medicationOrder);
    }


}
