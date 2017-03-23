package no.pharmacy.web.prescriptions;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import no.pharmacy.core.Money;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationDispenseRepository;

public class DispenseOrderController extends HttpServlet {

    private MedicationDispenseRepository prescriptionRepository;
    MedicationRepository medicationRepository;

    public DispenseOrderController(MedicationDispenseRepository medicationDispenseRepository,
            MedicationRepository medicationRepository) {
        this.prescriptionRepository = medicationDispenseRepository;
        this.medicationRepository = medicationRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DispenseOrder dispenseOrder = prescriptionRepository.getDispenseOrderById(req.getPathInfo().substring(1));

        DispenseOrderView view = new DispenseOrderView(dispenseOrder);
        Document doc = view.dispenseOrderView();
        resp.setContentType("text/html");
        doc.writeTo(resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DispenseOrder order = prescriptionRepository.getDispenseOrderById(req.getParameter("orderId"));

        for (MedicationDispense medicationDispense : order.getMedicationDispenseList()) {
            String productId = req.getParameter("medicationOrder[" + medicationDispense.getId() + "][productId]");

            String price = req.getParameter("medicationOrder[" + medicationDispense.getId() + "][" + productId + "][price]");

            Medication medication = medicationRepository.findByProductId(productId).get();

            medicationDispense.setMedication(medication);
            medicationDispense.setPrice(Money.from(price));

            prescriptionRepository.update(medicationDispense);
        }

        resp.sendRedirect(req.getRequestURI());
    }

}
