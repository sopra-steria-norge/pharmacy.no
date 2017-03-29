package no.pharmacy.test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eaxy.Element;
import org.eaxy.Namespace;
import org.eaxy.Validator;

import lombok.Getter;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationSource;
import no.pharmacy.medicationorder.PrescriptionsSource;

public class FakeReseptFormidler implements PrescriptionsSource {

    private static final Namespace M92 = new Namespace("http://www.kith.no/xmlstds/eresept/m92/2013-10-08", "M92");

    private Validator validator = new Validator(new String[] {
            "R1408-eResept-M9.1-4-2014-04-07/ER-M91-2013-10-08.xsd",
            "R1408-eResept-M9.1-4-2014-04-07/ER-M92-2013-10-08.xsd",
            "R1408-eResept-M9.1-4-2014-04-07/ER-M93-2010-06-04.xsd",
            "R1408-eResept-M9.1-4-2014-04-07/ER-M94-2010-07-01.xsd",
    });

    @Getter
    private final List<MessageLogEntry> messageLog = new ArrayList<>();

    private final Map<String, List<MedicationOrder>> prescriptionsForPerson = new HashMap<>();

    private final Map<String, MedicationOrder> prescriptionsById = new HashMap<>();

    private final MedicationSource medicationSource;

    private final PharmaTestData testData = new PharmaTestData();

    public FakeReseptFormidler(MedicationSource medicationSource) {
        this.medicationSource = medicationSource;
    }

    public MedicationOrder addPrescription(String nationalId, Medication product) {
        MedicationOrder medicationOrder = createMedicationOrder(product);
        this.prescriptionsForPerson.computeIfAbsent(nationalId, s -> new ArrayList<>())
            .add(medicationOrder);
        this.prescriptionsById.put(medicationOrder.getPrescriptionId(), medicationOrder);
        medicationOrder.setDosageText(product.getDisplay() + "\n\n2 piller, morgen og kveld");
        return medicationOrder;
    }

    private MedicationOrder createMedicationOrder(Medication product) {
        MedicationOrder medicationOrder = new MedicationOrder(product);
        medicationOrder.setPrescriber(testData.sampleDoctor());
        medicationOrder.setPrescriptionId(UUID.randomUUID().toString());
        medicationOrder.setDateWritten(LocalDate.now().minusDays(PharmaTestData.random(14)));
        return medicationOrder;
    }

    @Override
    public List<? extends MedicationOrder> prescriptionsForPerson(String nationalId) {
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

    public Element processRequest(Element request) {
        return logResponse(validator.validate(createResponse(logRequest(validator.validate(request)))));
    }

    private Element logResponse(Element element) {
        this.messageLog.add(new MessageLogEntry("DIFA <- RF", Instant.now(), element));
        return element;
    }

    private Element logRequest(Element element) {
        this.messageLog.add(new MessageLogEntry("DIFA -> RF", Instant.now(), element));
        return element;
    }

    private Element createResponse(Element request) {
        String nationalId = request.find("Fnr").first().text();

        Element prescriptionList = M92.el("Reseptliste");
        for (MedicationOrder medicationOrder : prescriptionsForPerson(nationalId)) {
            prescriptionList.add(M92.el("Reseptinfo",
                    M92.el("Forskrivningsdato", medicationOrder.getDateWritten().toString()),
                    M92.el("Fornavn", "fornavn"),
                    M92.el("Etternavn", "etternavn"),
                    M92.el("RekvirentId", medicationOrder.getPrescriber().getReference()),
                    M92.el("NavnRekvirent", medicationOrder.getPrescriber().getDisplay()),
                    M92.el("NavnFormStyrke", medicationOrder.getMedication().getDisplay()),
                    M92.el("EndretFarmasoyt"),
                    M92.el("ReseptId", medicationOrder.getPrescriptionId()),
                    M92.el("Status"),
                    M92.el("Vergeinnsynsreservasjon", "false")
                    ));
        }
        return prescriptionList;
    }

}
