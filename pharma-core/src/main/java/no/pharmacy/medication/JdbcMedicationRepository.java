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

import no.pharmacy.core.Money;
import no.pharmacy.infrastructure.ExceptionUtil;

public class JdbcMedicationRepository implements MedicationRepository, MedicationSource {

    private DataSource dataSource;

    public JdbcMedicationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public List<Medication> listAlternatives(Medication medication) {
        if (medication.getExchangeGroupId() == null) {
            return new ArrayList<>();
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("select * from medications where exchange_group_id = ? order by display asc")) {
                stmt.setString(1, medication.getExchangeGroupId());
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
    public List<Medication> list(int offset, int count, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("select * from medications where exchange_group_id is not null order by display asc  limit ? offset ?")) {
            stmt.setInt(1, count);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Medication> results = new ArrayList<>();

                while (rs.next()) {
                    results.add(read(rs));
                }

                return results;
            }
        }
    }

    public Optional<Medication> findByProductId(String productId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("select * from medications where product_id = ?")) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(read(rs));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    private Medication read(ResultSet rs) throws SQLException {
        Medication medication = new Medication();
        medication.setDisplay(rs.getString("display"));
        medication.setProductId(rs.getString("product_id"));
        medication.setTrinnPrice(Money.from(rs.getBigDecimal("trinn_price")));
        medication.setRetailPrice(Money.from(rs.getBigDecimal("retail_price")));
        medication.setExchangeGroupId(rs.getString("exchange_group_id"));
        return medication;
    }

    public void save(Medication medication, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("delete from medications where product_id = ?")) {
            stmt.setString(1, medication.getProductId());
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = conn.prepareStatement("insert into medications (product_id, display, trinn_price, retail_price, exchange_group_id) values (?, ?, ?, ?, ?)")) {
            stmt.setString(1, medication.getProductId());
            stmt.setString(2, medication.getDisplay());
            Money trinnPrice = medication.getTrinnPrice();
            stmt.setBigDecimal(3, trinnPrice != null ? trinnPrice.toBigDecimal() : null);
            Money retailPrice = medication.getRetailPrice();
            stmt.setBigDecimal(4, retailPrice != null ? retailPrice.toBigDecimal() : null);
            stmt.setString(5, medication.getExchangeGroupId());
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Medication> getMedication(String productId) {
        try (Connection conn = dataSource.getConnection()) {
            return findByProductId(productId, conn);
        } catch (SQLException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

}
