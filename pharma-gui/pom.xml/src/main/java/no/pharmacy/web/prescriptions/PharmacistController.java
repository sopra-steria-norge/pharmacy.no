package no.pharmacy.web.prescriptions;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationDispenseRepository;


public class PharmacistController extends HttpServlet {

    private MedicationDispenseRepository medicationDispenseRepository;
    private MedicationRepository medicationRepository;

    public PharmacistController(MedicationDispenseRepository medicationDispenseRepository,
            MedicationRepository medicationRepository) {
        this.medicationDispenseRepository = medicationDispenseRepository;
        this.medicationRepository = medicationRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo() != null && req.getPathInfo().length() > 1) {
            String orderId = req.getPathInfo().substring(1);
            DispenseOrder dispenseOrder = medicationDispenseRepository.getDispenseOrderById(orderId);

            resp.setContentType("text/html");
            new PharmacistDispenseOrderView(dispenseOrder).dispenseOrderView().writeTo(resp.getWriter());
        } else {

            Document doc = Xml.readResource("/pharma-webapp/pharmacist/index.html.template");


            Element results = doc.find("...", "#dispenseOrders").first();
            for (DispenseOrder dispenseOrder : medicationDispenseRepository.listReadyForPharmacist()) {
                results.add(Xml.el("div",
                            Xml.el("a")
                                .attr("href", dispenseOrder.getIdentifier())
                                .text(dispenseOrder.getIdentifier())));
            }

            resp.setContentType("text/html");
            doc.writeTo(resp.getWriter());
        }
    }

}
