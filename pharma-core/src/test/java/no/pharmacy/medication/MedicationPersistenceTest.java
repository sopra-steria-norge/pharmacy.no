package no.pharmacy.medication;


import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.UUID;

import javax.sql.DataSource;

import org.eaxy.Xml;
import org.junit.Test;

import no.pharmacy.test.FakeMedicationSource;
import no.pharmacy.test.TestDataSource;

public class MedicationPersistenceTest {

    private DataSource dataSource = TestDataSource.instance();
    private JdbcMedicationRepository repository = new JdbcMedicationRepository(dataSource);

    @Test
    public void retrievesSavedMedication() throws Exception {
        Medication medication = sampleMedication();
        assertThat(medication).hasNoNullFieldsOrProperties();

        try (Connection connection = dataSource.getConnection()) {
            repository.save(medication, connection);
            assertThat(repository.findByProductId(medication.getProductId(), connection).get())
                .isEqualToComparingFieldByField(medication);
        }
    }

    @Test
    public void listIncludesSavedMedication() throws Exception {
        Medication medication = sampleMedication();

        try (Connection connection = dataSource.getConnection()) {
            repository.save(medication, connection);
            assertThat(repository.list(0, 100, connection)).extracting(Medication::getProductId)
                .contains(medication.getProductId());
        }
    }

    private Medication sampleMedication() {
        Medication medication = new FakeMedicationSource().pickOne();
        medication.setExchangeGroupId(UUID.randomUUID().toString());
        medication.setXml(Xml.el("testElement", "Hello world").toXML());
        return medication;
    }

}
