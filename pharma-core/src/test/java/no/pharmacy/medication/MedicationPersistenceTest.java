package no.pharmacy.medication;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.eaxy.Xml;
import org.junit.Test;

import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class MedicationPersistenceTest {

    private JdbcMedicationRepository repository = new JdbcMedicationRepository(TestDataSource.medicationInstance());

    @Test
    public void shouldRetrieveSavedMedication() throws Exception {
        Medication medication = sampleMedication();
        assertThat(medication).hasNoNullFieldsOrProperties();

        repository.save(medication);
        assertThat(repository.findByProductId(medication.getProductId()).get())
            .isEqualToComparingFieldByField(medication);
    }

    @Test
    public void shouldListSavedMedication() throws Exception {
        Medication medication = sampleMedication();

        repository.save(medication);
        assertThat(repository.list(0, 100))
            .extracting(Medication::getProductId)
            .contains(medication.getProductId());
    }

    @Test
    public void shouldListInteractionsOnPartialAtcCodes() {
        MedicationInteraction interaction = sampleInteraction("N06BA04", "N03AB");
        repository.save(interaction);
        assertThat(repository.listInteractions("N03AB05"))
            .contains(interaction);
    }

    @Test
    public void shouldListSavedInteractions() {
        MedicationInteraction interaction = sampleInteraction("N06BA05", "N06AB02");
        assertThat(interaction).hasNoNullFieldsOrProperties();
        repository.save(interaction);

        List<MedicationInteraction> interactions = repository.listInteractions(interaction.getSubstanceCodes().get(0));
        assertThat(interactions).contains(interaction);
        assertThat(interactions.get(0)).isEqualToComparingFieldByField(interaction);

        assertThat(repository.listInteractions("X99AB99")).isEmpty();
    }

    @Test
    public void shouldRetrieveInteractionsForMedication() {
        MedicationInteraction interaction = sampleInteraction("N06AG02", "N06BA");
        repository.save(interaction);

        Medication medication = new Medication();
        medication.setProductId("500595");
        medication.setDisplay("Ritalin");
        medication.setSubstance("N06BA04");
        repository.save(medication);

        assertThat(repository.getMedication(medication.getProductId()).get().getInteractions())
            .contains(interaction);
    }

    private MedicationInteraction sampleInteraction(String first, String second) {
        MedicationInteraction interaction = new MedicationInteraction();
        interaction.setId(UUID.randomUUID().toString());
        interaction.getSubstanceCodes().add(first);
        interaction.getSubstanceCodes().add(second);
        interaction.setSeverity(PharmaTestData.pickOne(MedicalInteractionSeverity.class));
        interaction.setClinicalConsequence(PharmaTestData.lorum());
        interaction.setInteractionMechanism(PharmaTestData.lorum());
        return interaction;
    }

    private Medication sampleMedication() {
        Medication medication = new Medication();
        medication.setProductId(PharmaTestData.sampleProductId());
        medication.setDisplay(PharmaTestData.lorum());
        medication.setSubstitutionGroup(UUID.randomUUID().toString());
        medication.setGtin(PharmaTestData.sampleGtin());
        medication.setSubstance(PharmaTestData.pickOneOf("A12AX", "N01BB02", "J06BA01", "J01CA08"));
        medication.setTrinnPrice(PharmaTestData.samplePrice());
        medication.setXml(Xml.el("testElement", "Hello world").toXML());
        return medication;
    }

}
