package no.pharmacy.dispense;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString(of={"warning", "action"})
public class MedicationDispenseAction {

    @Getter
    private final MedicationOrderWarning warning;

    @Getter @Setter
    private String remark;

    @Getter @Setter
    private String action;

    public MedicationDispenseAction(MedicationOrderWarning warning, String remark, String action) {
        this.warning = warning;
        this.remark = remark;
        this.action = action;
    }

    public boolean isAddressed() {
        return action != null && !action.isEmpty();
    }

    public String getWarningCode() {
        return warning.getCode();
    }

    public String getInteractionId() {
        return warning.getInteraction().getId();
    }

}
