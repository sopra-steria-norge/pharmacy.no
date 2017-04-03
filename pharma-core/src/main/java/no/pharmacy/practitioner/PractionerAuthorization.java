package no.pharmacy.practitioner;

public enum PractionerAuthorization {
    PHARMACIST("FA"), PHARMACIST2("FA2"), NURSE("SP"), DOCTOR("LE"), VETERINARIAN("VE");

    private String code;

    private PractionerAuthorization(String code) {
        this.code = code;
    }

    public static PractionerAuthorization getValue(String code) {
        for (PractionerAuthorization value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown " +
                PractionerAuthorization.class.getName() + " code " + code);
    }



}
