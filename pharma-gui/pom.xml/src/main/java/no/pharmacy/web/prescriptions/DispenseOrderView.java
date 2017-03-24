package no.pharmacy.web.prescriptions;

import java.io.IOException;
import java.util.Objects;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.core.Money;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.medication.Medication;
import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationOrder;

public class DispenseOrderView implements HtmlView {

    protected DispenseOrder dispenseOrder;

    public DispenseOrderView(DispenseOrder dispenseOrder) {
        this.dispenseOrder = dispenseOrder;
    }

    @Override
    public Document createView() throws IOException {
        Document doc = Xml.readResource("/pharma-webapp/dispense-order/index.html.template");

        String medicationOrderTemplate = doc.find("...", "#medicationOrderTemplate").first().elements().iterator().next().toXML();


        Element medicationOrders = doc.find("...", "#medicationOrders").first();

        for (MedicationDispense dispense : dispenseOrder.getMedicationDispenses()) {
            medicationOrders.add(displayMedicationDispense(medicationOrderTemplate, dispense));
        }

        displayPrices(doc);
        doc.find("...", "#technicalControl").first().attr("href", "./" + dispenseOrder.getIdentifier() + "/technicalControl");
        return doc;
    }

    protected void displayPrices(Document doc) {
        Money refundTotal = dispenseOrder.getRefundTotal();
        if (refundTotal != null) {
            doc.find("...", "#totalRefund").first().text(refundTotal.toString());
            doc.find("...", "#copay").first().text(dispenseOrder.getPatientTotal().toString());
            doc.find("...", "#uncoveredAmount").first().text(dispenseOrder.getUncoveredTotal().toString());
        }
    }

    protected Element displayMedicationDispense(String medicationOrderTemplate, MedicationDispense dispense) {
        Element orderElement = Xml.xml(medicationOrderTemplate).getRootElement();
        MedicationOrder medicationOrder = dispense.getAuthorizingPrescription();
        if (medicationOrder.getDateWritten() != null) {
            orderElement.find("...", ".dateWritten").first().text(medicationOrder.getDateWritten().toString());
        }
        orderElement.find("...", ".prescriber").first().text(medicationOrder.getPrescriber().getDisplay());
        orderElement.find("...", ".prescribedMedication").first().text(medicationOrder.getMedication().getDisplay());

        orderElement.find("...", ".dosageText").first().text(medicationOrder.getDosageText());

        String dosageTextName = "medicationOrder[" + dispense.getId() + "][printedDosageText]";
        orderElement.find("...", ".printedDosageText").first().name(dosageTextName).text(dispense.getPrintedDosageText());
        Element alternativeMedications = orderElement.find("...", ".alternativeMedications").first();
        for (Medication alternativeMedication : medicationOrder.getAlternatives()) {
            alternativeMedications.add(createMedicationOption(dispense.getId(), dispense, alternativeMedication));
        }
        return orderElement;
    }

    protected Element createMedicationOption(Long dispenseId, MedicationDispense dispense, Medication medication) {
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
        if (Objects.equals(dispense.getMedication(), medication)) {
            productIdField.checked(true);
            priceInput.val(dispense.getPrice().format());
        }
        return Xml.el("li",
                Xml.el("label",
                    productIdField,
                    Xml.el("span", medication.getDisplay()),
                    Xml.el("span", "Trinnpris " + medication.getTrinnPrice()),
                    productName,
                    Xml.el("div",
                            Xml.text("Utsalgspris: "),
                            priceInput)
                                .addClass("medicationPrice"),
                    productDetails));
    }
}
