package no.pharmacy.organization;

import java.net.URL;
import java.util.List;

public interface HealthcareServiceRepository {

    List<HealthcareService> listPharmacies();

    void save(HealthcareService organization);

    void update(HealthcareService organization);

    HealthcareService retrieve(String herNumber);

    long lastImportTime(URL source);

    void updateLastImportTime(long lastImportTime, URL source);


}
