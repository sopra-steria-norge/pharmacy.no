package no.pharmacy.medication;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

public interface MedicationRepository {

    List<Medication> list(int offset, int count);

    DataSource getDataSource();

    List<Medication> listAlternatives(Medication medication);

    Optional<Medication> findByProductId(String productId);

}
