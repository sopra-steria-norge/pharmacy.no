package no.pharmacy.test;

import java.util.ArrayList;
import java.util.List;
import org.eaxy.Element;
import org.eaxy.Namespace;

import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.medicationorder.MedicationOrderSummary;
import no.pharmacy.medicationorder.PrescriptionGateway;
import no.pharmacy.medicationorder.PrescriptionsSource;

public class FakePrescriptionGateway implements PrescriptionGateway, PrescriptionsSource {

    private static final Namespace M91 = new Namespace("http://www.kith.no/xmlstds/eresept/m91/2013-10-08", "M91");

    private FakeReseptFormidler fakeReseptFormidler;

    public FakePrescriptionGateway(FakeReseptFormidler fakeReseptFormidler) {
        this.fakeReseptFormidler = fakeReseptFormidler;
    }

    @Override
    public List<? extends MedicationOrderSummary> prescriptionsForPerson(String nationalId) {
        return requestMedicationOrdersToDispense(null, nationalId, "12342");
    }

    @Override
    public MedicationOrder getById(String id) {
        return fakeReseptFormidler.getById(id);
    }

    @Override
    public List<MedicationOrderSummary> requestMedicationOrdersToDispense(String purpose, String nationalId, String employeeId) {
        Element orderListRequest = createOrderListRequest(purpose, nationalId, employeeId);
        Element orderListResponse = fakeReseptFormidler.processRequest(orderListRequest);

        return decodeMedicationOrderListResponse(orderListResponse);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MedicationOrder cancelMedicationOrderDispense(String prescriptionId, String referenceNumber,
            String employeeId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void completeDispense(MedicationDispense dispense, String employeeId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancelDispense(String reason, String referenceNumber, String employeeId) {
        // TODO Auto-generated method stub

    }

}
