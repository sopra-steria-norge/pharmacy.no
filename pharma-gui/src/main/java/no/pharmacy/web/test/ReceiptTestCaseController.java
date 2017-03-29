package no.pharmacy.web.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;

public class ReceiptTestCaseController extends HttpServlet {

    private FakeReseptFormidler simulatedReseptFormidler;
    private MedicationRepository medicationRepository;
    private PharmaTestData testData = new PharmaTestData();

    public ReceiptTestCaseController(FakeReseptFormidler simulatedReseptFormidler, MedicationRepository medicationRepository) {
        this.simulatedReseptFormidler = simulatedReseptFormidler;
        this.medicationRepository = medicationRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = null;
        HashMap<Object,Object> flash = (HashMap<Object,Object>)req.getSession().getAttribute("flash");
        if (flash != null) {
            req.getSession().removeAttribute("flash");
            message = (String) flash.get("message");
        }

        Document doc = Xml.readResource("/pharma-testrig-webapp/index.html.template");

        if (message != null) {
            doc.find("...", "#flashMessage").first().text(message);
        }

        Element unusedIds = doc.find("...", "#unusedIds").first();
        for (String nationalId : testData.unusedNationalIds(50)) {
            unusedIds.add(Xml.el("option", Xml.attr("value", nationalId), Xml.attr("label", nationalId)));
        }

        Element medicationSelect = doc.find("...", "[name=productId]").first();
        List<Medication> sampleMedications = sampleMedications();
        for (Medication medication : sampleMedications) {
            medicationSelect.add(Xml.el("option", Xml.attr("value", medication.getProductId()),
                    Xml.attr("label", medication.getDisplay())));
        }

        resp.setContentType("text/html");
        doc.writeTo(resp.getWriter());
    }

    private List<Medication> sampleMedications() {
        try (Connection conn = medicationRepository.getDataSource().getConnection()) {
            return medicationRepository.list(0, 1000);
        } catch (SQLException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nationalId = req.getParameter("nationalId");
        String scenario = req.getParameter("scenario");
        HashMap<Object, Object> flash = new HashMap<>();
        if ("drugInteraction".equals(scenario)) {
            simulatedReseptFormidler.addPrescription(nationalId, "500595");
            simulatedReseptFormidler.addPrescription(nationalId, "466813");
        } else {
            simulatedReseptFormidler.addPrescription(nationalId, req.getParameter("productId"));
        }
        flash.put("message", "La inn resept for " + nationalId);
        flash.put("patient", nationalId);
        req.getSession().setAttribute("flash", flash);

        resp.sendRedirect(req.getRequestURI());
    }

}
