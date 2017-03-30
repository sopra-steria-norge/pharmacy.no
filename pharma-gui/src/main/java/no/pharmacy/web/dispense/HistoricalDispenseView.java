package no.pharmacy.web.dispense;

import java.io.IOException;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Node;
import org.eaxy.Xml;
import org.eaxy.html.Xhtml;

import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.MedicationDispense;

public class HistoricalDispenseView implements HtmlView {

    private DispenseOrder dispenseOrder;

    public HistoricalDispenseView(DispenseOrder dispenseOrder) {
        this.dispenseOrder = dispenseOrder;
    }

    @Override
    public Document createView() throws IOException {
        Document doc = Xml.readResource("/pharma-webapp/dispense-order/historical.html.template");

        doc.find("...", "#patient").first().text(dispenseOrder.getPatient().getDisplay());

        String medicationOrderTemplate = doc.find("...", "#medicationDispenseTemplate").first().elements().iterator().next().toXML();

        Element medicationOrders = doc.find("...", "#medicationDispenses").first();
        for (MedicationDispense dispense : dispenseOrder.getMedicationDispenses()) {
            medicationOrders.add(displayMedicationDispense(medicationOrderTemplate, dispense));
        }

        displayPrices(doc);

        doc.find("...", "#customerSignature").first().attr("src", dispenseOrder.getCustomerSignature());

        return doc;
    }

    private Node displayMedicationDispense(String medicationOrderTemplate, MedicationDispense dispense) {
        Xhtml dispenseEl = Xhtml.parse(medicationOrderTemplate);
        dispenseEl.find("...", ".dispensedMedication").first().text(dispense.getMedication().getDisplay());
        dispenseEl.find("...", ".prescribedMedication").first().text(dispense.getAuthorizingPrescription().getMedication().getDisplay());
        dispenseEl.find("...", ".prescriber").first().text(dispense.getAuthorizingPrescription().getPrescriber().getDisplay());
        dispenseEl.find("...", ".dateWritten").first().text(dispense.getAuthorizingPrescription().getDateWritten().toString());
        dispenseEl.find("...", ".dosageText").first().text(dispense.getAuthorizingPrescription().getDosageText());
        dispenseEl.find("...", ".printedDosageText").first().text(dispense.getPrintedDosageText());
        return dispenseEl.getRootElement();
    }

    private void displayPrices(Document doc) {
        doc.find("...", "#totalRefund").first().text(dispenseOrder.getRefundTotal().toString());
        doc.find("...", "#copay").first().text(dispenseOrder.getPatientTotal().toString());
        doc.find("...", "#uncoveredAmount").first().text(dispenseOrder.getUncoveredTotal().toString());
    }
}
