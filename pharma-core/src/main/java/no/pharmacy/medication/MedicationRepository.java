package no.pharmacy.medication;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

public interface MedicationRepository {

    List<Medication> list(int offset, int count, Connection conn) throws SQLException;

    DataSource getDataSource();

    List<Medication> listAlternatives(Medication medication);

}
