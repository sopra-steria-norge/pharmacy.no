package no.pharmacy.test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Namespace;
import org.eaxy.Xml;

import no.pharmacy.core.Reference;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.medicationorder.MedicationOrderSummary;
import no.pharmacy.medicationorder.PrescriptionGateway;
import no.pharmacy.medicationorder.PrescriptionsSource;

public class FakePrescriptionGateway implements PrescriptionGateway, PrescriptionsSource {

    private static final Namespace M91 = new Namespace("http://www.kith.no/xmlstds/eresept/m91/2013-10-08", "M91");
    private static final Namespace M93 = new Namespace("http://www.kith.no/xmlstds/eresept/m93/2010-06-04", "M93");
    private static final Namespace M10 = new Namespace("http://www.kith.no/xmlstds/eresept/m10/2013-10-08", "M10");
    private static final Namespace UTL = new Namespace("http://www.kith.no/xmlstds/eresept/utlevering/2013-10-08", "UTL");
    private static final Namespace KITH = new Namespace("http://www.kith.no/xmlstds/felleskomponent1", "KITH");

    private FakeReseptFormidler fakeReseptFormidler;

    private MedicationRepository medicationRepository;

    public FakePrescriptionGateway(FakeReseptFormidler fakeReseptFormidler, MedicationRepository medicationRepository) {
        this.fakeReseptFormidler = fakeReseptFormidler;
        this.medicationRepository = medicationRepository;
    }

    @Override
    public List<? extends MedicationOrderSummary> prescriptionsForPerson(String nationalId) {
        if (nationalId == null) {
            return new ArrayList<>();
        }
        return requestMedicationOrdersToDispense(null, nationalId, "12342");
    }

    @Override
    public MedicationOrder getById(String id) {
        return startMedicationOrderDispense(id, null, "1234");
    }

    @Override
    public List<MedicationOrderSummary> requestMedicationOrdersToDispense(String purpose, String nationalId, String employeeId) {
        Element orderListRequest = createOrderListRequest(purpose, nationalId, employeeId);
        Element orderListResponse = fakeReseptFormidler.processRequest(orderListRequest);


        return decodeMedicationOrderListResponse(orderListResponse);
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
        for (Element prescriptionInfo : orderListResponse.find("Reseptinfo")) {
            MedicationOrderSummary medicationOrderSummary = new MedicationOrderSummary();
            medicationOrderSummary.setPrescriptionId(prescriptionInfo.find("ReseptId").first().text());
            medicationOrderSummary.setMedicationName(prescriptionInfo.find("NavnFormStyrke").first().text());
            summary.add(medicationOrderSummary);
        }
        return summary;
    }

    private Element createOrderListRequest(String purpose, String nationalId, String employeeId) {
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
    public MedicationOrder startMedicationOrderDispense(String prescriptionId, String referenceNumber,
            String employeeId) {
        Element dispenseMedicationOrderRequest = createDispenseMedicationOrderRequest(prescriptionId, employeeId);

        Element medicationOrderResponse = fakeReseptFormidler.processRequest(dispenseMedicationOrderRequest);
        Document prescriptionDocument = findPrescriptionDocument(medicationOrderResponse);
        MedicationOrder medicationOrder = new MedicationOrder();
        String productId = prescriptionDocument.find("Document", "RefDoc", "Content", "Resept", "ReseptDokLegemiddel", "Forskrivning", "Legemiddelpakning", "Varenr").first().text();
        medicationOrder.setMedication(medicationRepository.findByProductId(productId).get());
        medicationOrder.setDateWritten(LocalDate.parse(prescriptionDocument.find("Document", "RefDoc", "Content", "Resept", "Forskrivningsdato").first().text()));
        medicationOrder.setDosageText(prescriptionDocument.find("Document", "RefDoc", "Content", "Resept", "ReseptDokLegemiddel", "Forskrivning", "DosVeiledEnkel").first().text());
        medicationOrder.setPrescriptionId(prescriptionId);
        Element prescriber = prescriptionDocument.find("MsgInfo", "Sender", "Organisation", "HealthcareProfessional").first();
        medicationOrder.setPrescriber(new Reference(prescriber.find("Ident", "Id").first().text(),
                prescriber.find("GivenName").first().text() + " " + prescriber.find("FamilyName").first().text()));

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
    public void completeDispense(MedicationDispense dispense, String employeeId) {
        Element request = createMedicationDispenseRequest(dispense, employeeId);

        fakeReseptFormidler.processRequest(request);

        fakeReseptFormidler.addDispense(dispense);
    }

    private Element createMedicationDispenseRequest(MedicationDispense dispense, String employeeId) {
        return M10.el("Utleveringsrapport",
                UTL.el("Utlevering",
                        UTL.el("ReseptId", dispense.getAuthorizingPrescription().getPrescriptionId()),
                        UTL.el("Utleveringsdato", LocalDate.now().toString()),
                        UTL.el("Annullering", "false"),
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
