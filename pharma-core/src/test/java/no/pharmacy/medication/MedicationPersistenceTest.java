package no.pharmacy.medication;


import static org.assertj.core.api.Assertions.assertThat;

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

        repository.save(medication);
        assertThat(repository.findByProductId(medication.getProductId()).get())
            .isEqualToComparingFieldByField(medication);
    }

    @Test
    public void listIncludesSavedMedication() throws Exception {
        Medication medication = sampleMedication();

        repository.save(medication);
        assertThat(repository.list(0, 100)).extracting(Medication::getProductId)
            .contains(medication.getProductId());
    }

    private Medication sampleMedication() {
        Medication medication = new FakeMedicationSource().pickOne();
        medication.setSubstitutionGroup(UUID.randomUUID().toString());
        medication.setXml(Xml.el("testElement", "Hello world").toXML());
        return medication;
    }

}
