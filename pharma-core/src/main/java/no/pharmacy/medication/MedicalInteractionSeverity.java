package no.pharmacy.medication;

import lombok.Getter;

public enum MedicalInteractionSeverity {
    SEVERE("1", "Avoid"), SERIOUS("2", "Take precautions"), INFO("3", "No action needed");

    private final String code;

    @Getter
    private final String description;

    private MedicalInteractionSeverity(String code, String description) {
        this.code = code;
        this.description = description;
    }

    static MedicalInteractionSeverity byValue(String code) {
        for (MedicalInteractionSeverity severity : values()) {
            if (severity.code.equals(code)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Illegal MedicalInteractionSeverity " + code);
    }
}
