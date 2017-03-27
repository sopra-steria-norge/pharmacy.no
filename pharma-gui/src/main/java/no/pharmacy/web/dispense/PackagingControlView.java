package no.pharmacy.web.dispense;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.MedicationDispense;

public class PackagingControlView implements HtmlView {

    private DispenseOrder order;
    private Map<MedicationDispense, String> dosageTextBarcodes = new HashMap<>();
    private Map<MedicationDispense, String> packagingBarcodes = new HashMap<>();
    private boolean failed;

    public PackagingControlView(DispenseOrder order) {
        this.order = order;
    }

    @Override
    public Document createView() throws IOException {
        Document doc = Xml.readResource("/pharma-webapp/dispense-order/technical-control.html.template");
        Element dispensesEl = doc.find("...", "#medicationDispenses").first();

        doc.find("...", "#orderId").first().val(order.getIdentifier());

        String dispenseTemplate = doc.find("...", "#medicationDispenseTemplate").first().elements().iterator().next().toXML();
        for (MedicationDispense dispense : order.getMedicationDispenses()) {

            String prefix = "dispense[" + dispense.getId() + "]";
            Element dispenseEl = Xml.xml(dispenseTemplate).getRootElement();

            String dosageTextBarcode = dosageTextBarcodes.get(dispense);
            Element scannedDosageEl = dispenseEl.find("...", ".scannedDosageTextBarcode").first();
            scannedDosageEl.name(prefix + "[dosageTextBarcode]").val(dosageTextBarcode);
            if (dosageTextBarcode != null && !dosageTextBarcode.equals(dispense.getDosageBarcode())) {
                scannedDosageEl.addClass("error");
                failed = true;
            }

            String packagingBarcode = packagingBarcodes.get(dispense);
            Element scannedPackagingEl = dispenseEl.find("...", ".scannedPackagingBarcode").first();
            scannedPackagingEl.name(prefix + "[packagingBarcode]").val(packagingBarcode);
            if (packagingBarcode != null && !packagingBarcode.equals(dispense.getMedication().getGtin())) {
                scannedPackagingEl.addClass("error");
                failed = true;
            }

            dispenseEl.find("...", ".prescribedMedication").first().text(dispense.getMedication().getDisplay());
            dispenseEl.find("...", ".printedDosageText").first().text(dispense.getPrintedDosageText());
            dispenseEl.find("...", ".dosageText").first().text(dispense.getAuthorizingPrescription().getDosageText());
            dispenseEl.find("...", ".expectedPackagingBarcode").first().text(dispense.getMedication().getGtin());
            dispenseEl.find("...", ".expectedDosageTextBarcode").first().text(dispense.getDosageBarcode());
            dispensesEl.add(dispenseEl);
        }
        return doc;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setPackagingBarcode(MedicationDispense dispense, String packagingBarcode) {
        this.packagingBarcodes.put(dispense, packagingBarcode);
    }

    public void setDispenseDosageTextBarcode(MedicationDispense dispense, String dosageTextBarcode) {
        this.dosageTextBarcodes.put(dispense, dosageTextBarcode);
    }

}
