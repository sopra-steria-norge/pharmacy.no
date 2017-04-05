package no.pharmacy.organization;

import java.io.InputStream;
import java.util.List;

public interface HealthcareServiceRepository {

    List<HealthcareService> listPharmacies();

    void save(HealthcareService organization);

    void update(HealthcareService organization);

    void refresh(InputStream input);

}
