package no.pharmacy.web.dispense;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import no.pharmacy.core.Money;
import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.DispenseOrderService;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationRepository;

public class DispenseOrderController extends HttpServlet {

    private MedicationDispenseRepository prescriptionRepository;
    MedicationRepository medicationRepository;
    private DispenseOrderService dispenseOrderService;

    public DispenseOrderController(MedicationDispenseRepository medicationDispenseRepository, MedicationRepository medicationRepository,
            DispenseOrderService dispenseOrderService) {
        this.prescriptionRepository = medicationDispenseRepository;
        this.medicationRepository = medicationRepository;
        this.dispenseOrderService = dispenseOrderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] parts = req.getPathInfo().split("/");
        DispenseOrder dispenseOrder = prescriptionRepository.getDispenseOrderById(parts[1]);

        if (dispenseOrder.isDispensed()) {
            HistoricalDispenseView view = new HistoricalDispenseView(dispenseOrder);
            resp.setContentType("text/html");
            view.createView().writeTo(resp.getWriter());
        } else if (parts.length <= 2) {
            DispenseOrderView view = new DispenseOrderView(dispenseOrder);
            resp.setContentType("text/html");
            view.createView().writeTo(resp.getWriter());
        } else if (parts[2].equals("technicalControl")) {
            PackagingControlView view = new PackagingControlView(dispenseOrder);
            resp.setContentType("text/html");
            view.createView().writeTo(resp.getWriter());
        } else if (parts[2].equals("dispense")) {
            DispenseView view = new DispenseView(dispenseOrder);
            resp.setContentType("text/html");
            view.createView().writeTo(resp.getWriter());
        } else {
            super.doGet(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] parts = req.getPathInfo().split("/");
        DispenseOrder order = prescriptionRepository.getDispenseOrderById(parts[1]);

        if (parts.length <= 2) {
            for (MedicationDispense medicationDispense : order.getMedicationDispenses()) {
                String dosageText = req.getParameter("medicationOrder[" + medicationDispense.getId() + "][printedDosageText]");
                medicationDispense.setPrintedDosageText(dosageText);

                String productId = req.getParameter("medicationOrder[" + medicationDispense.getId() + "][productId]");
                if (productId == null) continue;

                if (productId.isEmpty()) {
                    medicationDispense.setMedication(null);
                    medicationDispense.setPrice(null) ;
                    medicationDispense.setConfirmedByPharmacist(false);
                    medicationDispense.setPackagingControlled(false);
                } else {
                    String price = req.getParameter("medicationOrder[" + medicationDispense.getId() + "][" + productId + "][price]");
                    Medication medication = medicationRepository.findByProductId(productId).get();
                    medicationDispense.setMedication(medication);
                    medicationDispense.setPrice(Money.from(price));
                    medicationDispense.setConfirmedByPharmacist(false);
                    medicationDispense.setPackagingControlled(false);
                }
            }

            order.createWarnings();
            for (MedicationDispense medicationDispense : order.getMedicationDispenses()) {
                prescriptionRepository.update(medicationDispense);
            }

            resp.sendRedirect(req.getRequestURI());
        } else if (parts[2].equals("technicalControl")) {
            PackagingControlView view = new PackagingControlView(order);

            for (MedicationDispense dispense : order.getMedicationDispenses()) {
                String dosageTextBarcode = req.getParameter("dispense[" + dispense.getId() + "][dosageTextBarcode]");
                String packagingBarcode = req.getParameter("dispense[" + dispense.getId() + "][packagingBarcode]");
                view.setDispenseDosageTextBarcode(dispense, dosageTextBarcode);
                view.setPackagingBarcode(dispense, packagingBarcode);
            }

            Document doc = view.createView();

            if (view.isFailed()) {
                resp.setContentType("text/html");
                doc.writeTo(resp.getWriter());
            } else {
                for (MedicationDispense dispense : order.getMedicationDispenses()) {
                    dispense.setPackagingControlled(true);
                    prescriptionRepository.update(dispense);
                }

                String uri = req.getRequestURI();
                resp.sendRedirect(uri.substring(0,  uri.lastIndexOf('/')));
            }
        } else if (parts[2].equals("dispense")) {
            order.setCustomerSignature(req.getParameter("customerSignature"));

            dispenseOrderService.completeDispenseOrder(order);

            String uri = req.getRequestURI();
            resp.sendRedirect(uri.substring(0,  uri.lastIndexOf('/')));
        } else {

            super.doPost(req, resp);
        }
    }

}
