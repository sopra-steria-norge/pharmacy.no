package no.pharmacy.web.prescriptions;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrderWarning;
import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationOrder;

public class PharmacistDispenseOrderView extends DispenseOrderView {

    public PharmacistDispenseOrderView(DispenseOrder dispenseOrder) {
        super(dispenseOrder);
    }

    @Override
    protected void displayPrices(Document doc) {
        super.displayPrices(doc);
    }

    @Override
    protected Element displayMedicationDispense(String medicationOrderTemplate, MedicationDispense dispense) {
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
        for (MedicationOrderWarning warning : dispense.getWarnings(this.dispenseOrder)) {
            Element pharmacistAction = Xml.el("div",
                    Xml.el("input").attr("placeholder", "Notat"),
                    Xml.el("select",
                        Xml.el("option", ""),
                        Xml.el("option", "Konferert med lege"),
                        Xml.el("option", "Konferert med pasient"),
                        Xml.el("option", "Ignorert")));
            warningsDiv.add(Xml.el("div",
                    Xml.el("h4", "Advarsel: " + warning.getInteraction().getSubstanceCodes() + " " + warning.getInteraction().getSeverity()),
                    Xml.el("div", "Interakasjon med " + warning.getInteractingDispense().getMedication().getDisplay() + " forskrevet av " +
                            warning.getInteractingDispense().getAuthorizingPrescription().getPrescriber().getDisplay()
                            + " den " + warning.getInteractingDispense().getAuthorizingPrescription().getDateWritten()),
                    Xml.el("div", warning.getInteraction().getClinicalConsequence()).addClass("clinicalConsquence"),
                    pharmacistAction));
        }
        return warningsDiv;
    }

}
