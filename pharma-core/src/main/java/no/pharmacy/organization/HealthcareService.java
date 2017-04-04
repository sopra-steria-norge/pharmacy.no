package no.pharmacy.organization;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

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
}
