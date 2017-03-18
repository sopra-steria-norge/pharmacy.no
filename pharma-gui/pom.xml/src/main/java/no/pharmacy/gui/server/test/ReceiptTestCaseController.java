package no.pharmacy.gui.server.test;

import java.io.IOException;
import java.io.InputStream;
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
import no.pharmacy.test.PharmaTestData;

public class ReceiptTestCaseController extends HttpServlet {

    private FakeReseptFormidler simulatedReseptFormidler;
    private MedicationRepository medicationRepository;

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

        Document doc = readResource("/pharma-testrig-webapp/index.html.template");

        if (message != null) {
            doc.find("...", "#flashMessage").first().text(message);
        }

        Element unusedIds = doc.find("...", "#unusedIds").first();
        for (String nationalId : PharmaTestData.unusedNationalIds(50)) {
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
            return medicationRepository.list(0, 100);
        } catch (SQLException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nationalId = req.getParameter("nationalId");
        simulatedReseptFormidler.addPrescription(nationalId, req.getParameter("productId"));
        HashMap<Object, Object> value = new HashMap<>();
        value.put("message", "La inn resept for " + nationalId);
        req.getSession().setAttribute("flash", value);

        resp.sendRedirect(req.getRequestURI());
    }

    private Document readResource(String name) throws IOException {
        InputStream input = getClass().getResourceAsStream(name);
        if (input == null) {
            throw new IllegalArgumentException("Can't load " + name);
        }
        return Xml.readAndClose(input);
    }


}
