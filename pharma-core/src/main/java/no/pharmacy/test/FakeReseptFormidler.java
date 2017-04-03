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
import java.util.stream.Collectors;

import org.eaxy.Element;
import org.eaxy.Namespace;
import org.eaxy.Validator;

import lombok.Getter;
import no.pharmacy.core.MessageGateway;
import no.pharmacy.core.PersonReference;
import no.pharmacy.core.Reference;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationSource;
import no.pharmacy.patient.PatientRepository;

public class FakeReseptFormidler implements MessageGateway {

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

    private Map<String, List<String>> printedDosageTexts = new HashMap<>();

    private final Map<String, MedicationOrder> prescriptionsById = new HashMap<>();

    private final PatientRepository patientRepository;

    private final MedicationSource medicationSource;

    public FakeReseptFormidler(MedicationSource medicationSource, PatientRepository patientRepository) {
        this.medicationSource = medicationSource;
        this.patientRepository = patientRepository;
    }

    public MedicationOrder addPrescription(String nationalId, Medication product, PersonReference prescriber) {
        Reference patient = patientRepository.findPatientByNationalId(nationalId);

        MedicationOrder medicationOrder = new MedicationOrder(product);
        medicationOrder.setPrescriber(prescriber);
        medicationOrder.setPrescriptionId(UUID.randomUUID().toString());
        medicationOrder.setDateWritten(LocalDate.now().minusDays(PharmaTestData.random(14)));
        // TODO: Lookup nationalId in patient repository
        medicationOrder.setSubject(patient);

        this.prescriptionsForPerson.computeIfAbsent(nationalId, s -> new ArrayList<>())
            .add(medicationOrder);
        this.prescriptionsById.put(medicationOrder.getPrescriptionId(), medicationOrder);
        medicationOrder.setDosageText(patient.getDisplay() + "\n\n2 piller, morgen og kveld");
        return medicationOrder;
    }

    public MedicationOrder addPrescription(String nationalId, String productId, PersonReference prescriber) {
        Medication medication = this.medicationSource.getMedication(productId).orElseThrow(() -> new IllegalArgumentException(productId));
        return addPrescription(nationalId, medication, prescriber);
    }

    @Override
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
            for (MedicationOrder medicationOrder : prescriptionsForPerson.getOrDefault(nationalId, new ArrayList<>())) {
                prescriptionList.add(M92.el("Reseptinfo",
                        M92.el("Forskrivningsdato", medicationOrder.getDateWritten().toString()),
                        M92.el("Fornavn", medicationOrder.getSubject().getDisplay()),
                        M92.el("Etternavn", medicationOrder.getSubject().getDisplay()),
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
            MedicationOrder order = prescriptionsById.get(request.find("ReseptId").first().text());

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
            String prescriptionId = request.find("Utlevering", "ReseptId").first().text();
            String dosageText = request.find("Utlevering", "ReseptDokLegemiddel", "Forskrivning").check().find("DosVeiledEnkel").firstTextOrNull();
            printedDosageTexts
                .computeIfAbsent(prescriptionId, s -> new ArrayList<>())
                .add(dosageText);


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

    private Element msgInfo(String type, PersonReference prescriber) {
        Element senderOrganization = HEAD.el("Organisation",
                HEAD.el("OrganisationName", "Reseptformidleren (test)"),
                HEAD.el("Ident", HEAD.el("Id", "965336796"), HEAD.el("TypeId").attr("V", "ENH")));
        if (prescriber != null) {
            senderOrganization.add(HEAD.el("HealthcareProfessional",
                    HEAD.el("FamilyName", prescriber.getFirstName()),
                    HEAD.el("GivenName", prescriber.getLastName()),
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

    public List<String> getPrintedDosageTexts(MedicationOrder medicationOrder) {

        getDispensesFor(medicationOrder.getPrescriptionId()).stream()
                .map(MedicationDispense::getPrintedDosageText)
                .collect(Collectors.toList());
        return printedDosageTexts.get(medicationOrder.getPrescriptionId());
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
