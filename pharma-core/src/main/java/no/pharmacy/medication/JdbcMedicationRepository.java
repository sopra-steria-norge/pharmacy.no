package no.pharmacy.medication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        return retrieveSingle("select * from medications where product_id = ?",
                Arrays.asList(productId), this::read);
    }

    private Medication read(ResultSet rs) throws SQLException {
        Medication medication = new Medication();
        medication.setDisplay(rs.getString("display"));
        medication.setProductId(rs.getString("product_id"));
        medication.setTrinnPrice(Money.from(rs.getBigDecimal("trinn_price")));
        medication.setSubstitutionGroup(rs.getString("exchange_group_id"));
        medication.setXml(rs.getString("xml"));
        return medication;
    }

    public void save(Medication medication) {
        executeUpdate("delete from medications where product_id = ?",
                Arrays.asList(medication.getProductId()));

        insertInto("medications")
            .value("product_id", medication.getProductId())
            .value("display", medication.getDisplay())
            .value("trinn_price", medication.getTrinnPrice())
            .value("exchange_group_id", medication.getSubstitutionGroup())
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
        return queryForList(query, Arrays.asList(1000, 0), this::read);
    }

    private static final Logger logger = LoggerFactory.getLogger(JdbcMedicationRepository.class);

    public void refresh() {
        if (isEmpty()) {
            logger.info("Refreshing medications from FEST");
            try (Connection conn = dataSource.getConnection()) {
                new DownloadMedicationsFromFest(this).downloadFestFile();
            } catch (Exception e) {
                throw ExceptionUtil.softenException(e);
            }
        }
    }

}
