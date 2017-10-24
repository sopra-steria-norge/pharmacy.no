package no.pharmacy.medicationorder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementPath;
import org.eaxy.Namespace;
import org.eaxy.Xml;
import org.eaxy.XmlFormatter;
import org.jcp.xml.dsig.internal.DigesterOutputStream;

import lombok.NonNull;
import lombok.SneakyThrows;
import no.pharmacy.core.MessageGateway;
import no.pharmacy.core.PersonReference;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.infrastructure.messages.EbXmlMessage;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.organization.HealthcareService;
import no.pharmacy.patient.PatientRepository;

public class RFPrescriptionGateway implements PrescriptionGateway {

    private static final Namespace HEAD = new Namespace("http://www.kith.no/xmlstds/msghead/2006-05-24", "HEAD");

    private static final Namespace M1 = new Namespace("http://www.kith.no/xmlstds/eresept/m1/2013-10-08", "M1");
    private static final Namespace M91 = new Namespace("http://www.kith.no/xmlstds/eresept/m91/2013-10-08", "M91");
    private static final Namespace M93 = new Namespace("http://www.kith.no/xmlstds/eresept/m93/2010-06-04", "M93");
    private static final Namespace M10 = new Namespace("http://www.kith.no/xmlstds/eresept/m10/2013-10-08", "M10");
    private static final Namespace F = new Namespace("http://www.kith.no/xmlstds/eresept/forskrivning/2013-10-08", "F");
    private static final Namespace UTL = new Namespace("http://www.kith.no/xmlstds/eresept/utlevering/2013-10-08", "UTL");
    private static final Namespace KITH = new Namespace("http://www.kith.no/xmlstds/felleskomponent1", "KITH");

    private MessageGateway messageGateway;

    private MedicationRepository medicationRepository;
    private PatientRepository patientRepository;

    public RFPrescriptionGateway(
            @NonNull MessageGateway messageGateway,
            @NonNull MedicationRepository medicationRepository,
            @NonNull PatientRepository patientRepository) {
        this.messageGateway = messageGateway;
        this.medicationRepository = medicationRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public List<MedicationOrderSummary> requestMedicationOrdersToDispense(String purpose, String nationalId, String employeeId) {
        if (nationalId == null) {
            return new ArrayList<>();
        }
        Element orderListResponse = messageGateway.processRequest(
                msgHead(msgInfo("ERM92", sender(), getReseptFormidleren()),
                        createOrderListRequest(nationalId, employeeId)));
        return decodeMedicationOrderListResponse(orderListResponse);
    }

    private Element sender() {
        return HEAD.el("Organisation",
                HEAD.el("OrganisationName", "Kjell, Drugs and Rock 'n' Roll "),
                HEAD.el("Ident", HEAD.el("Id", "80624"), HEAD.el("TypeId").attr("V", "HER")));
    }

    private Element getReseptFormidleren() {
        Element reseptFormidler = HEAD.el("Organisation",
                HEAD.el("OrganisationName", "Reseptformidleren (test)"),
                HEAD.el("Ident", HEAD.el("Id", "965336796"), HEAD.el("TypeId").attr("V", "ENH")));
        return reseptFormidler;
    }

    private Element msgHead(Element msgInfo, Element request) {
        return HEAD.el("MsgHead",
                msgInfo,
                HEAD.el("Document",
                        HEAD.el("RefDoc",
                                HEAD.el("MsgType").attr("V", "XML"),
                                HEAD.el("Content",
                                        request))));
    }

    private Element msgInfo(String type, Element sender, Element receiver) {
        return HEAD.el("MsgInfo",
            HEAD.el("Type").attr("V", type),
            HEAD.el("MIGversion", "v1.2 2006-05-24"),
            HEAD.el("GenDate", Instant.now().toString()),
            HEAD.el("MsgId", UUID.randomUUID().toString()),
            HEAD.el("Sender", sender),
            HEAD.el("Receiver", receiver));
    }

    private Document findPrescriptionDocument(Element orderListResponse) {
        for (Element content : orderListResponse.find("Document", "RefDoc", "Content").check()) {
            if (content.find("Base64Container").isPresent()) {
                Document nestedDoc = Xml.xml(new String(Base64.getDecoder().decode(content.find("Base64Container").first().text().getBytes())));

                String msgType = nestedDoc.find("MsgInfo", "Type").first().attr("V");
                if (msgType.equals("ERM1")) {
                    return nestedDoc;
                }
            }
        }
        throw new IllegalArgumentException("Can't find ERM1 document");
    }

    private List<MedicationOrderSummary> decodeMedicationOrderListResponse(Element orderListResponse) {
        List<MedicationOrderSummary> summary = new ArrayList<>();
        for (Element prescriptionInfo : orderListResponse.find("Document", "RefDoc", "Content", "Reseptliste", "Reseptinfo").check()) {
            MedicationOrderSummary medicationOrderSummary = new MedicationOrderSummary();
            medicationOrderSummary.setPrescriptionId(prescriptionInfo.find("ReseptId").first().text());
            medicationOrderSummary.setMedicationName(prescriptionInfo.find("NavnFormStyrke").first().text());
            medicationOrderSummary.setPrescriberName(prescriptionInfo.find("NavnRekvirent").first().text());
            medicationOrderSummary.setDateWritten(
                    LocalDate.parse(prescriptionInfo.find("Forskrivningsdato").first().text()));
            summary.add(medicationOrderSummary);
        }
        return summary;
    }

    private Element createOrderListRequest(String nationalId, String employeeId) {
        return M91.el("ForesporselReseptUtleverer",
                M91.el("Fnr", nationalId),
                M91.el("AlleResepter"),
                M91.el("AnsattId", employeeId),
                M91.el("InkluderVergeinnsynsreservasjon")
                );
    }

    @Override
    public List<MedicationOrderSummary> requestMedicationOrdersToDispense(String purpose, String birthDate,
            String firstName, String lastName, String employeeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MedicationOrderSummary> requestMedicationOrdersToDispenseByReference(String purpose,
            String referenceNumber, String employeeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MedicationOrder startMedicationOrderDispense(String prescriptionId, String referenceNumber, String employeeId) {
        Element medicationOrderResponse = messageGateway.processRequest(
                msgHead(
                        msgInfo("ERM93", sender(), getReseptFormidleren()),
                        createDispenseMedicationOrderRequest(prescriptionId, employeeId)));
        return decodeMedicationOrder(prescriptionId, medicationOrderResponse);
    }

    private MedicationOrder decodeMedicationOrder(String prescriptionId, Element medicationOrderResponse) {
        Document prescriptionDocument = findPrescriptionDocument(medicationOrderResponse);
        MedicationOrder medicationOrder = new MedicationOrder();
        Element prescription = prescriptionDocument.find("Document", "RefDoc", "Content", "Resept").first();
        String productId = prescription.find("ReseptDokLegemiddel", "Forskrivning", "Legemiddelpakning", "Varenr").first().text();
        medicationOrder.setMedication(medicationRepository.findByProductId(productId).get());
        medicationOrder.setDateWritten(LocalDate.parse(prescription.find("Forskrivningsdato").first().text()));
        medicationOrder.setDosageText(prescription.find("ReseptDokLegemiddel", "Forskrivning", "DosVeiledEnkel").first().text());
        medicationOrder.setPrescriptionId(prescriptionId);
        Element prescriber = prescriptionDocument.find("MsgInfo", "Sender", "Organisation", "HealthcareProfessional").first();
        medicationOrder.setPrescriber(new PersonReference(
                prescriber.find("Ident", "Id").first().text(),
                prescriber.find("GivenName").first().text(),
                prescriber.find("FamilyName").first().text()));
        String nationalId = prescriptionDocument.find("MsgInfo", "Patient", "Ident", "Id")
                .first().text();
        medicationOrder.setSubject(patientRepository.findPatientByNationalId(nationalId));
        return medicationOrder;
    }

    private Element createDispenseMedicationOrderRequest(String prescriptionId, String employeeId) {
        return M93.el("M93",
                M93.el("ReseptId", prescriptionId),
                M93.el("AnsattId", employeeId));
    }

    @Override
    public MedicationOrder cancelMedicationOrderDispense(String prescriptionId, String referenceNumber,
            String employeeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void completeDispense(MedicationDispense dispense, String employeeId, HealthcareService dispensingOrganization) {
        messageGateway.processRequest(signMessage(msgHead(
                msgInfo("ERM10", sender(), getReseptFormidleren()),
                createMedicationDispenseRequest(dispense, employeeId)), dispensingOrganization));
    }

    private static Namespace XMLDSIG = new Namespace("http://www.w3.org/2000/09/xmldsig#");

    @SneakyThrows({IOException.class, GeneralSecurityException.class})
    private Element signMessage(Element msgHead, HealthcareService dispensingOrganization) {
        if (dispensingOrganization.getCertificateAsBase64() == null) {
            return msgHead;
        }

        MessageDigest md = MessageDigest.getInstance(EbXmlMessage.getDigestMethod("http://www.w3.org/2000/09/xmldsig#rsa-sha1"));
        try(OutputStreamWriter writer = new OutputStreamWriter(new DigesterOutputStream(md))) {
            XmlFormatter.canonical("http://www.w3.org/TR/2001/REC-xml-c14n-20010315").format(writer, new ElementPath(null, msgHead));
            writer.flush();
        }
        String digestMessage = Base64.getMimeEncoder().encodeToString(md.digest());

        msgHead.add(XMLDSIG.el("Signature",
                XMLDSIG.el("SignedInfo",
                        XMLDSIG.el("CanonicalizationMethod").attr("Algorithm", ""),
                        XMLDSIG.el("SignatureMethod").attr("Algorithm", ""),
                        XMLDSIG.el("Reference",
                                XMLDSIG.el("Transforms",
                                        XMLDSIG.el("Transform").attr("Algorithm", "http://www.w3.org/2000/09/xmldsig#enveloped-signature"),
                                        XMLDSIG.el("Transform").attr("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315")
                                        ),
                                XMLDSIG.el("DigestMethod").attr("Algorithm", "http://www.w3.org/2000/09/xmldsig#rsa-sha1"),
                                XMLDSIG.el("DigestValue", digestMessage))),
                XMLDSIG.el("SignatureValue"),
                XMLDSIG.el("KeyInfo", XMLDSIG.el("X509Data",
                        XMLDSIG.el("X509Certificate", dispensingOrganization.getCertificateAsBase64())))));
        return msgHead;
    }

    private Element createMedicationDispenseRequest(MedicationDispense dispense, String employeeId) {
        return M10.el("Utleveringsrapport",
                UTL.el("Utlevering",
                        UTL.el("ReseptId", dispense.getAuthorizingPrescription().getPrescriptionId()),
                        UTL.el("Utleveringsdato", LocalDate.now().toString()),
                        UTL.el("Annullering", "false"),
                        M1.el("ReseptDokLegemiddel",
                                M1.el("Varegruppekode").attr("V", "L"),
                                M1.el("Reiterasjon", "1"),
                                F.el("Forskrivning",
                                        F.el("DosVeiledEnkel", dispense.getPrintedDosageText()),
                                        F.el("Legemiddelpakning",
                                                F.el("NavnFormStyrke", dispense.getMedication().getDisplay()),
                                                F.el("Reseptgruppe"),
                                                F.el("Varenr", dispense.getMedication().getProductId())
                                                ))
                                ),
                        UTL.el("Utleverer",
                                UTL.el("HerId", KITH.el("Id", "3454"), KITH.el("TypeId")),
                                UTL.el("Navn"))),
                M10.el("ReservasjonRapportFastlege", "false"),
                M10.el("AnsattId", employeeId));
    }

    @Override
    public void cancelDispense(String reason, String referenceNumber, String employeeId) {
        // TODO Auto-generated method stub

    }

}
