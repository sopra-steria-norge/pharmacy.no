package no.pharmacy.dispense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import no.pharmacy.core.Money;
import no.pharmacy.infrastructure.CryptoUtil;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationInteraction;

@EqualsAndHashCode(of={"id", "medication", "authorizingPrescription" })
public class MedicationDispense {

    @Getter @Setter
    private Long id;

    public MedicationDispense(MedicationOrder authorizingPrescription) {
        this.authorizingPrescription = authorizingPrescription;
        //this.medication = authorizingPrescription.getMedication();
        this.printedDosageText = authorizingPrescription.getDosageText();
    }

    public MedicationDispense() {
    }

    @Getter @Setter
    private Medication medication;

    @Getter @Setter
    private Money price;

    @Getter @Setter
    private String printedDosageText;

    @Getter @Setter
    private MedicationOrder authorizingPrescription;

    private Map<String, MedicationDispenseAction> actions = new HashMap<>();

    @Getter @Setter
    private boolean confirmedByPharmacist = false;

    @Getter @Setter
    private boolean packagingControlled;

    @Getter
    private boolean dispensed;

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "(medication=" + medication + ",price=" + price
                + ",authorizingPrescription=" + getPrescriptionId();
    }

    private String getPrescriptionId() {
        return authorizingPrescription != null
                ? "prescription-" + authorizingPrescription.getId()
                : null;
    }

    public String getRefundGroup() {
        return authorizingPrescription.getRefundGroup();
    }

    public Money getCoveredAmount() {
        return getMedication().getCoveredAmount(getPrice());
    }

    public Money getUncoveredAmount() {
        return getMedication().getUncoveredAmount(getPrice());
    }

    private List<MedicationInteraction> getInteractions(MedicationDispense historicalDispense) {
        if (medication == null) {
            return new ArrayList<>();
        }
        return medication.getInteractionsWith(historicalDispense.getMedication());
    }

    public void setAction(MedicationOrderWarning warning, String remark, String action) {
        actions.put(warning.getInteraction().getId(), new MedicationDispenseAction(warning, remark, action));
    }

    public boolean isPharmacistControlComplete() {
        for (MedicationDispenseAction action : this.actions.values()) {
            if (!action.isAddressed()) {
                return false;
            }
        }
        return confirmedByPharmacist;
    }

    void createWarnings(MedicationHistory history) {
        for (MedicationDispense historicalDispense : history.getDispenses()) {
            if (historicalDispense.equals(this)) continue;
            for (MedicationInteraction interaction : getInteractions(historicalDispense)) {
                MedicationOrderWarning warning = new MedicationOrderWarning(historicalDispense, interaction);
                actions.put(interaction.getId(), new MedicationDispenseAction(warning, null, null));
            }
        }
    }

    void addMedicationDispenseAction(MedicationDispenseAction action) {
        this.actions.put(action.getWarningCode(), action);
    }

    public Collection<MedicationDispenseAction> getWarningActions() {
        return this.actions.values();
    }

    public String getMedicationId() {
        return medication != null ? medication.getProductId() : null;
    }

    public String getDosageBarcode() {
        byte[] hash = CryptoUtil.sha256(getPrintedDosageText());
        int i = (hash[0]&0x7f) << 24 | (hash[1]&0xff) << 16 | (hash[2]&0xff) << 8 | (hash[3]&0xff);
        return String.valueOf(i);
    }

    public void setDispensed() {
        this.dispensed = true;
    }

}
