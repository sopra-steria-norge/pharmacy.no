package no.pharmacy.practitioner;

// Kodeverk 9060 - https://volven.no/produkt.asp?open_f=true&id=292576&catID=3&subID=8&subCat=61&oid=9060
public enum PractionerAuthorization {
    PHARMACIST("FA"), PHARMACIST1("FA1"), PHARMACIST2("FA2"), NURSE("SP"), DOCTOR("LE"), VETERINARIAN("VE"),
    PHYSIOTHERAPIST("FT"), PHARMACY_TECHNICIAN("AT");

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
