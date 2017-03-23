package no.pharmacy.medication;

public enum MedicalInteractionSeverity {
    SEVERE("1");

    private String code;

    private MedicalInteractionSeverity(String code) {
        this.code = code;
    }
}
