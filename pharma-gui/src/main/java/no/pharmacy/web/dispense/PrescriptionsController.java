package no.pharmacy.web.dispense;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.core.Reference;
import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.medicationorder.MedicationOrderSummary;
import no.pharmacy.medicationorder.PrescriptionGateway;
import no.pharmacy.patient.PatientRepository;

public class PrescriptionsController extends HttpServlet {

    private PrescriptionGateway prescriptionsGateway;
    private MedicationDispenseRepository medicationDispenseRepository;
    private PatientRepository patientRepository;

    public PrescriptionsController(
            PrescriptionGateway prescriptionsGateway,
            MedicationDispenseRepository medicationDispenseRepository,
            PatientRepository patientRepository) {
        this.prescriptionsGateway = prescriptionsGateway;
        this.medicationDispenseRepository = medicationDispenseRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nationalId = req.getParameter("nationalId");
        if (nationalId == null) {
            Document doc = Xml.readResource("/pharma-webapp/index.html.template");
            resp.setContentType("text/html");
            doc.writeTo(resp.getWriter());
            return;
        }

        Reference patient = patientRepository.findPatientByNationalId(nationalId);
        if ("journal".equals(req.getParameter("action"))) {
            Document doc = showDispenseHistoryView(nationalId, patient,
                    medicationDispenseRepository.historicalDispensesForPerson(patient));
            resp.setContentType("text/html");
            doc.writeTo(resp.getWriter());
        } else {
            Document doc = showDispenseCreationView(nationalId, patient,
                    prescriptionsGateway.requestMedicationOrdersToDispense(null, nationalId, "1234"));
            resp.setContentType("text/html");
            doc.writeTo(resp.getWriter());
        }
    }

    private Document showDispenseHistoryView(String nationalId, Reference patient, List<DispenseOrder> dispenseOrders) throws IOException {
        Document doc = Xml.readResource("/pharma-webapp/index.html.template");

        if (patient != null) {
            doc.find("...", "#historyForNationalId").first().val(nationalId);

            Element results = doc.find("...", "#historicalOrders").first();
            results.add(Xml.el("div", "Resepthistorikk for for " + patient.getDisplay()));

            for (DispenseOrder order : dispenseOrders) {
                results.add(Xml.el("div",
                        Xml.el("label",
                            Xml.el("input")
                                .type("radio").name("dispenseOrderId").val(order.getIdentifier()),
                            Xml.el("span", order.getIdentifier()))));
            }
        }
        return doc;
    }

    private Document showDispenseCreationView(String nationalId, Reference patient, List<? extends MedicationOrderSummary> orders) throws IOException {
        Document doc = Xml.readResource("/pharma-webapp/index.html.template");

        if (nationalId != null) {
            doc.find("...", "#prescriptionsForNationalId").first().val(nationalId);

            Element results = doc.find("...", "#results").first();
            results.add(Xml.el("div", "Resepter for for " + patient.getDisplay()));

            results.add(Xml.el("input").name("nationalId").val(nationalId).type("hidden"));

            for (MedicationOrderSummary order : orders) {
                results.add(Xml.el("div",
                        Xml.el("label",
                            Xml.el("input")
                                .type("checkbox").name("prescriptionId").val(order.getPrescriptionId()),
                            Xml.el("span", order.getPrescriberName() + ": "),
                            Xml.el("span", order.getMedicationName()))));
            }
        }
        return doc;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("journal".equals(req.getParameter("action"))) {
            resp.sendRedirect("/dispenseOrder/" + req.getParameter("dispenseOrderId"));
        } else {
            Reference patient = patientRepository.findPatientByNationalId(req.getParameter("nationalId"));

            DispenseOrder dispenseOrder = new DispenseOrder();
            dispenseOrder.setPatient(patient);

            for (String id : req.getParameterValues("prescriptionId")) {
                dispenseOrder.addMedicationOrder(prescriptionsGateway.startMedicationOrderDispense(id, null, "124"));
            }

            medicationDispenseRepository.saveDispenseOrder(dispenseOrder);
            resp.sendRedirect("/dispenseOrder/" + dispenseOrder.getIdentifier());
        }
    }
}
