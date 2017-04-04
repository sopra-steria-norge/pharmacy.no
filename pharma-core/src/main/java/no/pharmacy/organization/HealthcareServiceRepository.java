package no.pharmacy.organization;

import java.util.List;

public interface HealthcareServiceRepository {

    List<HealthcareService> listPharmacies();

    void refresh(String filename);

    void save(HealthcareService organization);

    void update(HealthcareService organization);

}
