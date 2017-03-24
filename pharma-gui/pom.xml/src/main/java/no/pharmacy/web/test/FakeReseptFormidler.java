package no.pharmacy.web.test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationSource;
import no.pharmacy.order.MedicationOrder;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.web.prescriptions.PrescriptionsSource;

public class FakeReseptFormidler implements PrescriptionsSource {

    private final Map<String, List<MedicationOrder>> prescriptionsForPerson = new HashMap<>();

    private final Map<String, MedicationOrder> prescriptionsById = new HashMap<>();

    private final MedicationSource medicationSource;

    public FakeReseptFormidler(MedicationSource medicationSource) {
        this.medicationSource = medicationSource;
    }

    public MedicationOrder addPrescription(String nationalId, Medication product) {
        MedicationOrder medicationOrder = createMedicationOrder(nationalId, product);
        this.prescriptionsForPerson.computeIfAbsent(nationalId, s -> new ArrayList<>())
            .add(medicationOrder);
        this.prescriptionsById.put(medicationOrder.getPrescriptionId(), medicationOrder);
        medicationOrder.setDosageText(product.getDisplay() + "\n\n2 piller, morgen og kveld");
        return medicationOrder;
    }

    private MedicationOrder createMedicationOrder(String nationalId, Medication product) {
        MedicationOrder medicationOrder = new MedicationOrder(nationalId, product);
        medicationOrder.setPrescriptionId(UUID.randomUUID().toString());
        medicationOrder.setDateWritten(LocalDate.now().minusDays(PharmaTestData.random(14)));
        return medicationOrder;
    }

    @Override
    public List<MedicationOrder> prescriptionsForPerson(String nationalId) {
        return this.prescriptionsForPerson.getOrDefault(nationalId, new ArrayList<>());
    }

    @Override
    public MedicationOrder getById(String id) {
        return this.prescriptionsById.get(id);
    }

    public MedicationOrder addPrescription(String nationalId, String productId) {
        return addPrescription(nationalId,
                this.medicationSource.getMedication(productId).orElseThrow(() -> new IllegalArgumentException(productId)));
    }

}
