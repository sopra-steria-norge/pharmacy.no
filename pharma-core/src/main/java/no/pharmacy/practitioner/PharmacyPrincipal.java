package no.pharmacy.practitioner;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import no.pharmacy.organization.HealthcareService;

public class PharmacyPrincipal {

    @Getter @Setter
    private String name, displayName, hprNumber;

    @Getter
    private List<HealthcareService> organizations = new ArrayList<>();

    @Getter @Setter
    private Practitioner practitioner;

    @Getter @Setter
    private String jwtToken;

}
