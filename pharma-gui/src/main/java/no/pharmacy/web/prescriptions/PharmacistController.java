package no.pharmacy.web.prescriptions;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationDispenseAction;
import no.pharmacy.dispense.MedicationOrderWarning;
import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationDispenseRepository;


public class PharmacistController extends HttpServlet {

    private MedicationDispenseRepository repository;

    public PharmacistController(MedicationDispenseRepository medicationDispenseRepository) {
        this.repository = medicationDispenseRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo() != null && req.getPathInfo().length() > 1) {
            String orderId = req.getPathInfo().substring(1);
            DispenseOrder dispenseOrder = repository.getDispenseOrderById(orderId);

            resp.setContentType("text/html");
            new PharmacistDispenseOrderView(dispenseOrder).createView().writeTo(resp.getWriter());
        } else {

            Document doc = Xml.readResource("/pharma-webapp/pharmacist/index.html.template");


            Element results = doc.find("...", "#dispenseOrders").first();
            for (DispenseOrder dispenseOrder : repository.listReadyForPharmacist()) {
                results.add(Xml.el("div",
                            Xml.el("a")
                                .attr("href", dispenseOrder.getIdentifier())
                                .text(dispenseOrder.getIdentifier())));
            }

            resp.setContentType("text/html");
            doc.writeTo(resp.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo() != null && req.getPathInfo().length() > 1) {
            String orderId = req.getPathInfo().substring(1);
            DispenseOrder dispenseOrder = repository.getDispenseOrderById(orderId);

            for (MedicationDispense dispense : dispenseOrder.getMedicationDispenses()) {
                for (MedicationDispenseAction warningAction : dispense.getWarningActions()) {
                    MedicationOrderWarning warning = warningAction.getWarning();
                    String warningId = "dispense[" + dispense.getId() + "][warning][" + warning.getInteraction().getId() + "]";
                    String remark = req.getParameter(warningId + "[remark]");
                    String action = req.getParameter(warningId + "[action]");
                    dispense.setAction(warning, remark, action);
                }
                repository.update(dispense);
            }

            if (!dispenseOrder.isAllWarningsAddressed()) {
                resp.setContentType("text/html");
                PharmacistDispenseOrderView view = new PharmacistDispenseOrderView(dispenseOrder);
                view.setDisplayMissingActions(true);
                view.createView().writeTo(resp.getWriter());
            } else {
                resp.sendRedirect(req.getServletPath() + "/");
            }
        } else {
            super.doPost(req, resp);
        }
    }

}
