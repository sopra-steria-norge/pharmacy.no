package no.pharmacy.web.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.core.PersonReference;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.practitioner.Practitioner;
import no.pharmacy.practitioner.PractitionerRepository;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;

public class ReceiptTestCaseController extends HttpServlet {

    private FakeReseptFormidler simulatedReseptFormidler;
    private MedicationRepository medicationRepository;
    private PractitionerRepository practitionerRepository;

    private PharmaTestData testData = new PharmaTestData();

    public ReceiptTestCaseController(FakeReseptFormidler simulatedReseptFormidler, MedicationRepository medicationRepository, PractitionerRepository practitionerRepository) {
        this.simulatedReseptFormidler = simulatedReseptFormidler;
        this.medicationRepository = medicationRepository;
        this.practitionerRepository = practitionerRepository;
    }

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = null;
        HashMap<Object,Object> flash = getFlash(req);
        if (flash != null) {
            req.getSession().removeAttribute("flash");
            message = (String) flash.get("message");
        }

        Document doc = Xml.readResource("/pharma-testrig-webapp/index.html.template");

        if (message != null) {
            doc.find("...", "#flashMessage").first().text(message);
        }

        Random random = new Random(324523L);
        Element unusedIds = doc.find("...", "#unusedIds").first();
        for (String nationalId : testData.unusedNationalIds(random, 50)) {
            unusedIds.add(Xml.el("option", Xml.attr("value", nationalId), Xml.attr("label", nationalId)));
        }

        Element prescriber = doc.find("...", "#prescriber").first();
        for (PersonReference doctor : practitionerRepository.listDoctors()) {
            prescriber.add(Xml.el("option").val(doctor.getReference())
                    .text(doctor.getDisplay() + " (" + doctor.getReference() + ")"));
        }
        PharmaTestData.pickOne(prescriber.elements()).selected(true);

        Element medicationSelect = doc.find("...", "[name=productId]").first();
        for (Medication medication : medicationRepository.list(0, 1000)) {
            medicationSelect.add(Xml.el("option", Xml.attr("value", medication.getProductId()),
                    Xml.attr("label", medication.getDisplay())));
        }

        resp.setContentType("text/html");
        doc.writeTo(resp.getWriter());
    }

    @SuppressWarnings("unchecked")
    private HashMap<Object, Object> getFlash(HttpServletRequest req) {
        return (HashMap<Object,Object>)req.getSession().getAttribute("flash");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nationalId = req.getParameter("nationalId");
        String scenario = req.getParameter("scenario");
        Practitioner prescriber = practitionerRepository.getPractitioner(req.getParameter("prescriber")).get();

        HashMap<Object, Object> flash = new HashMap<>();
        if ("drugInteraction".equals(scenario)) {
            simulatedReseptFormidler.addPrescription(nationalId, "500595", prescriber.getReference());
            simulatedReseptFormidler.addPrescription(nationalId, "466813", prescriber.getReference());
        } else {
            simulatedReseptFormidler.addPrescription(nationalId, req.getParameter("productId"), prescriber.getReference());
        }
        flash.put("message", "La inn resept for " + nationalId);
        flash.put("patient", nationalId);
        req.getSession().setAttribute("flash", flash);

        resp.sendRedirect(req.getRequestURI());
    }

}
