package no.pharmacy.web.prescriptions;

import java.io.IOException;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationDispenseAction;
import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationOrder;

public class PharmacistDispenseOrderView implements HtmlView {

    private DispenseOrder dispenseOrder;

    private boolean displayMissingActions = false;

    public PharmacistDispenseOrderView(DispenseOrder dispenseOrder) {
        this.dispenseOrder = dispenseOrder;
    }

    @Override
    public Document createView() throws IOException {
        Document doc = Xml.readResource("/pharma-webapp/pharmacist/control.html.template");

        Element medicationOrders = doc.find("...", "#medicationOrders").first();

        for (MedicationDispense prescription : dispenseOrder.getMedicationDispenses()) {
            medicationOrders.add(displayMedicationDispense(prescription));
        }

        return doc;
    }

    private Element displayMedicationDispense(MedicationDispense dispense) {
        MedicationOrder medicationOrder = dispense.getAuthorizingPrescription();
        return Xml.el("div",
                Xml.el("h3", dispense.getMedication().getDisplay()),
                Xml.el("ul",
                        Xml.el("li", "Forskrevet av " + medicationOrder.getPrescriber().getDisplay()),
                        Xml.el("li", "Forskrevet dato " + medicationOrder.getDateWritten())),
                createInteractionWarnings(dispense));
    }

    private Element createInteractionWarnings(MedicationDispense dispense) {
        Element warningsDiv = Xml.el("div");
        for (MedicationDispenseAction action : dispense.getWarningActions()) {
            String warningId = "dispense[" + dispense.getId() + "][warning][" + action.getWarningCode() + "]";

            Element actionSelect = Xml.el("select",
                Xml.attr("name", warningId + "[action]"),
                Xml.el("option", "").val(""),
                Xml.el("option", "Konferert med lege").val("1"),
                Xml.el("option", "Konferert med pasient").val("2"),
                Xml.el("option", "Ignorert").val("0"));
            if (action.getAction() != null && !action.getAction().isEmpty()) {
                actionSelect.find("option[value=" + action.getAction() + "]").first().selected(true);
            }
            if (displayMissingActions && !action.isAddressed()) {
                actionSelect.addClass("error");
            }


            Element actionRemark = Xml.el("input").attr("placeholder", "Notat")
                    .name(warningId + "[remark]").val(action.getRemark());

            warningsDiv.add(Xml.el("div",
                    Xml.el("h4", "Advarsel: " + action.getWarning().getInteraction().getSubstanceCodes() + " " + action.getWarning().getInteraction().getSeverity()),
                    Xml.el("div", "Interakasjon med " + action.getWarning().displayInteractingDispense()),
                    Xml.el("div", action.getWarning().getInteraction().getClinicalConsequence()).addClass("clinicalConsquence"),
                    Xml.el("div", actionRemark, actionSelect)));
        }
        return warningsDiv;
    }

    public void setDisplayMissingActions(boolean displayMissingActions) {
        this.displayMissingActions = displayMissingActions;
    }

}
