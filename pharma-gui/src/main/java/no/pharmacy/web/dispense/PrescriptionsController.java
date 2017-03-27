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
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.patient.PatientRepository;

public class PrescriptionsController extends HttpServlet {


    private PrescriptionsSource prescriptionsSource;
    private MedicationDispenseRepository medicationDispenseRepository;
    private PatientRepository patientRepository;


    public PrescriptionsController(
            PrescriptionsSource prescriptionsSource,
            MedicationDispenseRepository medicationDispenseRepository,
            PatientRepository patientRepository) {
        this.prescriptionsSource = prescriptionsSource;
        this.medicationDispenseRepository = medicationDispenseRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nationalId = req.getParameter("nationalId");
        if ("journal".equals(req.getParameter("action"))) {
            Reference patient = patientRepository.findPatientByNationalId(nationalId);
            Document doc = showDispenseHistoryView(patient,
                    medicationDispenseRepository.historicalDispensesForPerson(patient));
            resp.setContentType("text/html");
            doc.writeTo(resp.getWriter());
        } else {
            Document doc = showDispenseCreationView(nationalId,
                    prescriptionsSource.prescriptionsForPerson(nationalId));
            resp.setContentType("text/html");
            doc.writeTo(resp.getWriter());
        }
    }

    private Document showDispenseHistoryView(Reference patient, List<DispenseOrder> dispenseOrders) throws IOException {
        Document doc = Xml.readResource("/pharma-webapp/index.html.template");

        if (patient != null) {
            Element results = doc.find("...", "#historicalOrders").first();
            results.text("Results for " + patient);

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

    private Document showDispenseCreationView(String nationalId, List<MedicationOrder> orders) throws IOException {
        Document doc = Xml.readResource("/pharma-webapp/index.html.template");


        if (nationalId != null) {
            Element results = doc.find("...", "#results").first();
            results.text("Results for " + nationalId);

            results.add(Xml.el("input").name("nationalId").val(nationalId).type("hidden"));

            for (MedicationOrder order : orders) {
                results.add(Xml.el("div",
                        Xml.el("label",
                            Xml.el("input")
                                .type("checkbox").name("prescriptionId").val(order.getPrescriptionId()),
                            //Xml.el("a", order.getPrescriber().getDisplay()).attr("href", "/practitioner/" + order.getPrescriber().getReference()),
                            Xml.el("span", order.getMedication().getDisplay()))));
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
                dispenseOrder.addMedicationOrder(prescriptionsSource.getById(id));
            }

            medicationDispenseRepository.saveDispenseOrder(dispenseOrder);
            resp.sendRedirect("/dispenseOrder/" + dispenseOrder.getIdentifier());
        }
    }
}
