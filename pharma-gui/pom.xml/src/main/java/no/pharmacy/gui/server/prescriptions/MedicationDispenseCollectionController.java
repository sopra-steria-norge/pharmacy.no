package no.pharmacy.gui.server.prescriptions;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.order.MedicationDispenseCollection;
import no.pharmacy.order.MedicationDispenseRepository;
import no.pharmacy.order.MedicationOrder;

public class MedicationDispenseCollectionController extends HttpServlet {

    private PrescriptionsSource reseptFormidler;
    private MedicationDispenseRepository medicationDispenseRepository;
    private MedicationRepository medicationRepository;

    public MedicationDispenseCollectionController(PrescriptionsSource reseptFormidler,
            MedicationDispenseRepository medicationDispenseRepository,
            MedicationRepository medicationRepository) {
        this.reseptFormidler = reseptFormidler;
        this.medicationDispenseRepository = medicationDispenseRepository;
        this.medicationRepository = medicationRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MedicationDispenseCollection collection = medicationDispenseRepository.getMedicationDispenseCollectionById(req.getPathInfo().substring(1));


        Document doc = readResource("/pharma-webapp/medication-dispense-collections/index.html.template");

        String medicationOrderTemplate = doc.find("...", "#medicationOrderTemplate").first().elements().iterator().next().toXML();

        Element medicationOrders = doc.find("...", "#medicationOrders").first();

        for (MedicationOrder medicationOrder : collection.getMedicationOrders()) {

            Element orderElement = Xml.xml(medicationOrderTemplate).getRootElement();
            orderElement.find("...", ".prescribedMedication").first().text(medicationOrder.getMedication().getDisplay());
            Element alternativeMedications = orderElement.find("...", ".alternativeMedications").first();
            for (Medication alternativeMedication : medicationRepository.listAlternatives(medicationOrder.getMedication())) {
                alternativeMedications.add(Xml.el("li",
                        Xml.el("input")
                            .type("radio").name("productId").val(alternativeMedication.getProductId()),
                        Xml.el("span", alternativeMedication.getDisplay())));

            }

            medicationOrders.add(orderElement);
        }

        resp.setContentType("text/html");
        doc.writeTo(resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPost(req, resp);
    }

    private Document readResource(String name) throws IOException {
        InputStream input = getClass().getResourceAsStream(name);
        if (input == null) {
            throw new IllegalArgumentException("Can't load " + name);
        }
        return Xml.readAndClose(input);
    }

}
