package no.pharmacy.test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eaxy.Element;
import org.eaxy.Namespace;
import org.eaxy.Validator;

import lombok.Getter;
import no.pharmacy.core.Reference;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationSource;
import no.pharmacy.medicationorder.PrescriptionsSource;

public class FakeReseptFormidler implements PrescriptionsSource {

    private static final Namespace F = new Namespace("http://www.kith.no/xmlstds/eresept/forskrivning/2013-10-08", "F");

    private static final Namespace M1 = new Namespace("http://www.kith.no/xmlstds/eresept/m1/2013-10-08", "M1");
    private static final Namespace M92 = new Namespace("http://www.kith.no/xmlstds/eresept/m92/2013-10-08", "M92");
    private static final Namespace M94 = new Namespace("http://www.kith.no/xmlstds/eresept/m94/2010-07-01", "M94");

    private static final Namespace HEAD = new Namespace("http://www.kith.no/xmlstds/msghead/2006-05-24", "HEAD");
    private static final Namespace bas = new Namespace("http://www.kith.no/xmlstds/base64container", "bas");

    private Validator validator = new Validator(new String[] {
            "felles/kith-base64.xsd",
            "felles/MsgHead-v1_2.xsd",
            "R0908-eResept-M1-M21-2013-10-08/ER-M1-2013-10-08.xsd",
            "R1408-eResept-M9.1-4-2014-04-07/ER-M91-2013-10-08.xsd",
            "R1408-eResept-M9.1-4-2014-04-07/ER-M92-2013-10-08.xsd",
            "R1408-eResept-M9.1-4-2014-04-07/ER-M93-2010-06-04.xsd",
            "R1408-eResept-M9.1-4-2014-04-07/ER-M94-2010-07-01.xsd",
            "R1308-eResept-M6-M8-M81-M10-M20-2015-09-22/ER-M10-2013-10-08.xsd",
    });

    @Getter
    private final List<MessageLogEntry> messageLog = new ArrayList<>();

    private final Map<String, List<MedicationOrder>> prescriptionsForPerson = new HashMap<>();

    private final Map<String, List<MedicationDispense>> dispensesForPrescription = new HashMap<>();

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
        if (request.tagName().equals("ForesporselReseptUtleverer")) {

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
        } else if (request.tagName().equals("M93")) {
            MedicationOrder order = getById(request.find("ReseptId").first().text());

            Element prescription = M1.el("Resept",
                    M1.el("Forskrivningsdato", order.getDateWritten().toString()),
                    M1.el("Utloper", LocalDate.now().plusMonths(1).toString()),
                    M1.el("ReseptDokLegemiddel",
                            M1.el("Varegruppekode").attr("V", "L"),
                            M1.el("Reiterasjon", "1"),
                            F.el("Forskrivning",
                                    F.el("DosVeiledEnkel", order.getDosageText()),
                                    F.el("Legemiddelpakning",
                                            F.el("NavnFormStyrke", order.getMedicationName()),
                                            F.el("Reseptgruppe"),
                                            F.el("Varenr", order.getMedication().getProductId())
                                            ))
                            ),
                    M1.el("OppdatertFest", LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant().toString())
                    );
            Element prescriptionDoc = HEAD.el("MsgHead",
                    msgInfo("ERM1", order.getPrescriber()), // TODO: Include patient
                    includedDocument(prescription));
            return HEAD.el("MsgHead",
                    msgInfo("ERM94", null),
                    includedDocument(M94.el("ReseptNedlasting", M94.el("Status"))),
                    encodedDocument(prescriptionDoc));
        } else if (request.tagName().equals("Utleveringsrapport")) {
            return HEAD.el("MsgHead",
                    msgInfo("M10", null),
                    HEAD.el("Document",
                            HEAD.el("RefDoc",
                                    HEAD.el("MsgType").attr("V", "XML"),
                                    HEAD.el("Content"))));
        } else {
            throw new IllegalArgumentException("Unknown request " + request);
        }
    }

    private Element encodedDocument(Element element) {
        validator.validate(element);
        return HEAD.el("Document",
                HEAD.el("RefDoc",
                        HEAD.el("MsgType").attr("V", "XML"),
                        HEAD.el("Content",
                                bas.el("Base64Container",
                                        Base64.getEncoder().encodeToString(element.toXML().getBytes())))));
    }

    private Element includedDocument(Element prescriptionDownload) {
        return HEAD.el("Document",
                HEAD.el("RefDoc",
                        HEAD.el("MsgType").attr("V", "XML"),
                        HEAD.el("Content",
                                prescriptionDownload)));
    }

    private Element msgInfo(String type, Reference prescriber) {
        Element senderOrganization = HEAD.el("Organisation",
                HEAD.el("OrganisationName", "Reseptformidleren (test)"),
                HEAD.el("Ident", HEAD.el("Id", "965336796"), HEAD.el("TypeId").attr("V", "ENH")));
        if (prescriber != null) {
            int lastNamePos = prescriber.getDisplay().lastIndexOf(" ");
            senderOrganization.add(HEAD.el("HealthcareProfessional",
                    HEAD.el("FamilyName", prescriber.getDisplay().substring(lastNamePos+1)),
                    HEAD.el("GivenName", prescriber.getDisplay().substring(0, lastNamePos)),
                    HEAD.el("Ident", HEAD.el("Id", prescriber.getReference()), HEAD.el("TypeId"))
                    ));
        }
        return HEAD.el("MsgInfo",
                HEAD.el("Type").attr("V", type),
                HEAD.el("MIGversion", "v1.2 2006-05-24"),
                HEAD.el("GenDate", Instant.now().toString()),
                HEAD.el("MsgId", UUID.randomUUID().toString()),
                HEAD.el("Sender", senderOrganization),
                HEAD.el("Receiver", HEAD.el("Organisation",
                        HEAD.el("OrganisationName", "Kjell, Drugs and Rock 'n' Roll "),
                        HEAD.el("Ident", HEAD.el("Id", "80624"), HEAD.el("TypeId").attr("V", "HER")))));
    }

    public List<MedicationDispense> getDispensesFor(MedicationOrder medicationOrder) {
        return getDispensesFor(medicationOrder.getPrescriptionId());
    }

    private List<MedicationDispense> getDispensesFor(String prescriptionId) {
        return dispensesForPrescription
                .getOrDefault(prescriptionId, new ArrayList<>());
    }

    public void addDispense(MedicationDispense dispense) {
        dispensesForPrescription
            .computeIfAbsent(dispense.getAuthorizingPrescription().getPrescriptionId(), s -> new ArrayList<>())
            .add(dispense);
    }

}
