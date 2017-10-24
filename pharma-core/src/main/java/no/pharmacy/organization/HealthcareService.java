package no.pharmacy.organization;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(of = { "id", "display" })
@ToString(of = { "id", "display" })
public class HealthcareService {

    @Getter @Setter
    private String id;

    @Getter @Setter
    private String display;

    @Getter @Setter
    private String municipalityCode;

    @Getter @Setter
    private String businessType;

    @Getter @Setter
    private Instant updatedAt;

    @Getter @Setter
    private String certificateAsBase64;

    public String getDN() {
        // "OU=" + display + ", O=NORSK MEDISINALDEPOT AS, C=NO"
        if (getChain() == null) {
            return "O=\"" + display + "-" + id + "\", C=NO";
        } else {
            return "OU=" + display + "-" + id + ", O=" + getChain() + ", C=NO";
        }
    }

    private String getChain() {
        if (display.startsWith("VITUS")) {
            return "NORSK MEDISINALDEPOT AS";
        } else if (display.startsWith("DITT APOTEK ")) {
            return "NORSK MEDISINALDEPOT AS";
        } else if (display.startsWith("APOTEK 1 ")) {
            return "APOTEK 1 KJEDEN";
        } else if (display.startsWith("BOOTS APOTEK ")) {
            return "BOOTS APOTEK KJEDEN";
        } else {
            return null;
        }
    }
}
