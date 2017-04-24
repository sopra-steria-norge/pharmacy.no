package no.pharmacy.dispense;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import no.pharmacy.medicationorder.MedicationOrderSummary;

public class MedicationOrderQuery {

    @Getter
    private UUID id;

    @Getter @Setter
    private List<MedicationOrderSummary> prescriptions = new ArrayList<>();

    public MedicationOrderQuery(String herNumber, String hprNumber) {
        id = UUID.randomUUID();
    }


}
