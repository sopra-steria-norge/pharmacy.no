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

    public void addPrescription(String nationalId, Medication product) {
        MedicationOrder medicationOrder = createMedicationOrder(nationalId, product);
        this.prescriptionsForPerson.computeIfAbsent(nationalId, s -> new ArrayList<>())
            .add(medicationOrder);
        this.prescriptionsById.put(medicationOrder.getPrescriptionId(), medicationOrder);
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

    public void addPrescription(String nationalId, String productId) {
        addPrescription(nationalId,
                this.medicationSource.getMedication(productId).orElseThrow(() -> new IllegalArgumentException(productId)));
    }

}
