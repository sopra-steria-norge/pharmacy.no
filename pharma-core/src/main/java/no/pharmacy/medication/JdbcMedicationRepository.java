package no.pharmacy.medication;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.core.Money;
import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class JdbcMedicationRepository extends JdbcSupport implements MedicationRepository, MedicationSource {

    private DataSource dataSource;

    public JdbcMedicationRepository(DataSource dataSource) {
        super(dataSource);
        this.dataSource = dataSource;
    }


    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public List<Medication> listAlternatives(Medication medication) {
        if (medication.getSubstitutionGroup() == null) {
            return new ArrayList<>();
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("select * from medications where exchange_group_id = ? order by display asc")) {
                stmt.setString(1, medication.getSubstitutionGroup());
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Medication> results = new ArrayList<>();

                    while (rs.next()) {
                        results.add(read(rs));
                    }

                    return results;
                }
            }
        } catch (SQLException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    @Override
    public List<Medication> list(int offset, int count) {
        return queryForList(
                "select * from medications where exchange_group_id is not null and trinn_price is not null order by display asc  limit ? offset ?",
                Arrays.asList(count, offset), this::read);
    }

    @Override
    public Optional<Medication> findByProductId(String productId) {
        // Cache!
        return retrieveSingle("select * from medications where product_id = ?",
                Arrays.asList(productId), this::read);
    }

    private Medication read(ResultSet rs) throws SQLException {
        Medication medication = new Medication();
        medication.setDisplay(rs.getString("display"));
        medication.setProductId(rs.getString("product_id"));
        medication.setGtin(rs.getString("gtin"));
        medication.setTrinnPrice(Money.from(rs.getBigDecimal("trinn_price")));
        medication.setSubstitutionGroup(rs.getString("exchange_group_id"));
        medication.setSubstance(rs.getString("substance"));
        medication.setXml(rs.getString("xml"));
        medication.getInteractions().addAll(listInteractions(medication.getSubstance()));
        return medication;
    }

    @Override
    public void save(Medication medication) {
        executeUpdate("delete from medications where product_id = ?",
                Arrays.asList(medication.getProductId()));

        insertInto("medications")
            .value("product_id", medication.getProductId())
            .value("gtin", medication.getGtin())
            .value("display", medication.getDisplay())
            .value("trinn_price", medication.getTrinnPrice())
            .value("exchange_group_id", medication.getSubstitutionGroup())
            .value("substance", medication.getSubstance())
            .value("xml", medication.getXml())
            .executeInsert()
            ;
    }

    @Override
    public Optional<Medication> getMedication(String productId) {
        return retrieveSingle("select * from medications where product_id = ?",
            Arrays.asList(productId), this::read);
    }

    public boolean isEmpty() {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("select count(*) from medications")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) == 0;
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }
        } catch (SQLException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    public List<Medication> list() {
        String query = "select * from medications where exchange_group_id is not null order by display asc  limit ? offset ?";
        return queryForList(query, Arrays.asList(10000, 0), this::read);
    }

    private static final Logger logger = LoggerFactory.getLogger(JdbcMedicationRepository.class);

    public void refresh(String festUrl) {
        if (isEmpty()) {
            logger.info("Refreshing medications from FEST");
            try {
                FestMedicationImporter importer = new FestMedicationImporter();
                importer.saveFest(new URL(festUrl), this);
            } catch (IOException e) {
                throw ExceptionUtil.softenException(e);
            }
        }
    }

    @Override
    public void save(MedicationInteraction interaction) {
        insertInto("medication_interactions")
            .value("id", interaction.getId())
            .value("severity", interaction.getSeverity())
            .value("clinical_consequence", interaction.getClinicalConsequence())
            .value("interaction_mechanism", interaction.getInteractionMechanism())
            .executeUpdate();

        for (String atcCode : interaction.getSubstanceCodes()) {
            insertInto("interacting_substance")
                .value("interaction_id", interaction.getId())
                .value("atc_code", atcCode)
                .executeUpdate();
        }
        interactionsByAtc.clear();
    }

    private static Map<String, List<MedicationInteraction>> interactionsByAtc = new HashMap<>();
    private static Map<String, MedicationInteraction> interactionsById = new HashMap<>();

    @Override
    public MedicationInteraction getInteraction(String id) {
        ensureInteractionCache();
        return interactionsById.get(id);
    }

    public synchronized List<MedicationInteraction> listInteractions(String atcCode) {
        ensureInteractionCache();

        String anatomicalGroup = atcCode.substring(0, 1);
        String therapeuticGroup = atcCode.substring(0, 3);
        String pharmacologicalGroup = atcCode.substring(0, 4);
        String chemicalGroup = atcCode.substring(0, 5);
        String substance = atcCode.length() >= 7 ? atcCode.substring(0, 7) : "N/A";
        List<MedicationInteraction> result = new ArrayList<>();
        List<MedicationInteraction> empty = new ArrayList<>();
        result.addAll(interactionsByAtc.getOrDefault(anatomicalGroup, empty));
        result.addAll(interactionsByAtc.getOrDefault(therapeuticGroup, empty));
        result.addAll(interactionsByAtc.getOrDefault(pharmacologicalGroup, empty));
        result.addAll(interactionsByAtc.getOrDefault(chemicalGroup, empty));
        result.addAll(interactionsByAtc.getOrDefault(substance, empty));
        return result;
    }


    private void ensureInteractionCache() {
        synchronized (interactionsByAtc) {
            if (interactionsByAtc.isEmpty()) {
                String query = "select * from medication_interactions i inner join interacting_substance s on i.id = s.interaction_id "
                        + "order by i.id";
                List<MedicationInteraction> interactions = queryForResultSet(query,
                        new ArrayList<>(), this::readInteractions);
                for (MedicationInteraction interaction : interactions) {
                    for (String substance : interaction.getSubstanceCodes()) {
                        interactionsByAtc
                            .computeIfAbsent(substance, s -> new ArrayList<>())
                            .add(interaction);
                    }
                    interactionsById.put(interaction.getId(), interaction);
                }
            }
        }
    }


    private List<MedicationInteraction> readInteractions(ResultSet rs) throws SQLException {
        String id = "";
        List<MedicationInteraction> result = new ArrayList<>();
        while (rs.next()) {
            if (!id.equals(rs.getString("id"))) {
                id = rs.getString("id");
                MedicationInteraction interaction = new MedicationInteraction();
                interaction.setId(id);
                interaction.setSeverity(MedicalInteractionSeverity.valueOf(rs.getString("severity")));
                interaction.setClinicalConsequence(rs.getString("clinical_consequence"));
                interaction.setInteractionMechanism(rs.getString("interaction_mechanism"));
                result.add(interaction);
            }
            result.get(result.size()-1).getSubstanceCodes().add(rs.getString("atc_code"));
        }
        return result;
    }




}
