package no.pharmacy.gui.server.prescriptions;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.core.Money;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationDispenseRepository;
import no.pharmacy.order.MedicationOrder;

public class DispenseOrderController extends HttpServlet {

    private MedicationDispenseRepository medicationDispenseRepository;
    private MedicationRepository medicationRepository;

    public DispenseOrderController(MedicationDispenseRepository medicationDispenseRepository,
            MedicationRepository medicationRepository) {
        this.medicationDispenseRepository = medicationDispenseRepository;
        this.medicationRepository = medicationRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DispenseOrder collection = medicationDispenseRepository.getMedicationDispenseCollectionById(req.getPathInfo().substring(1));

        Document doc = Xml.readResource("/pharma-webapp/medication-dispense-collections/index.html.template");

        String medicationOrderTemplate = doc.find("...", "#medicationOrderTemplate").first().elements().iterator().next().toXML();

        Element orderId = doc.find("...", "#orderId").first().val(collection.getIdentifier());
        orderId.text(collection.getIdentifier());

        Element medicationOrders = doc.find("...", "#medicationOrders").first();

        for (MedicationDispense prescription : collection.getMedicationDispenseList()) {
            MedicationOrder medicationOrder = prescription.getAuthorizingPrescription();
            Element orderElement = Xml.xml(medicationOrderTemplate).getRootElement();
            if (medicationOrder.getDateWritten() != null) {
                orderElement.find("...", ".dateWritten").first().text(medicationOrder.getDateWritten().toString());
            }
            orderElement.find("...", ".prescriber").first().text(medicationOrder.getPrescriber().getDisplay());
            orderElement.find("...", ".prescribedMedication").first().text(medicationOrder.getMedication().getDisplay());
            Element alternativeMedications = orderElement.find("...", ".alternativeMedications").first();
            for (Medication alternativeMedication : medicationRepository.listAlternatives(medicationOrder.getMedication())) {
                alternativeMedications.add(createMedicationOption(prescription.getId(), prescription, alternativeMedication));
            }

            medicationOrders.add(orderElement);
        }

        doc.find("...", "#totalRefund").first().text(collection.getRefundTotal().toString());
        doc.find("...", "#uncoveredAmount").first().text(collection.getPatientTotal().toString());

        resp.setContentType("text/html");
        doc.writeTo(resp.getWriter());
    }

    private Element createMedicationOption(Long dispenseId, MedicationDispense dispense, Medication medication) {
        Element productIdField = Xml.el("input")
                .type("radio").name("medicationOrder[" + dispenseId + "][productId]")
                .addClass("productSelect")
                .val(medication.getProductId());
        Element productName = Xml.el("a", "[info]")
                .addClass("medicationDetails")
                .attr("href", "#");
        Element priceInput = Xml.el("input")
            .type("number")
            .name("medicationOrder[" + dispenseId + "][" + medication.getProductId() + "][price]")
            .attr("step", "any");
        Element productDetails = Xml.el("div", "Details for " + medication.getDisplay())
                .addClass("medicationDescription");
        if (dispense.getMedication().equals(medication)) {
            productIdField.checked(true);
            priceInput.val(dispense.getPrice().format());
        }
        return Xml.el("li",
                Xml.el("label",
                    productIdField,
                    Xml.el("span", medication.getDisplay()),
                    productName,
                    Xml.el("div",
                            Xml.text("Utsalgspris:"),
                            priceInput)
                                .addClass("medicationPrice"),
                    productDetails));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DispenseOrder order = medicationDispenseRepository.getMedicationDispenseCollectionById(req.getParameter("orderId"));

        for (MedicationDispense medicationDispense : order.getMedicationDispenseList()) {
            String productId = req.getParameter("medicationOrder[" + medicationDispense.getId() + "][productId]");

            String price = req.getParameter("medicationOrder[" + medicationDispense.getId() + "][" + productId + "][price]");

            Medication medication = medicationRepository.findByProductId(productId).get();

            medicationDispense.setMedication(medication);
            medicationDispense.setPrice(Money.from(price));

            medicationDispenseRepository.update(medicationDispense);
        }

        resp.sendRedirect(req.getRequestURI());
    }

}
