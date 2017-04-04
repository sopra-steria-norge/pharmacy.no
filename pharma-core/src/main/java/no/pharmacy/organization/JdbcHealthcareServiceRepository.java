package no.pharmacy.organization;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class JdbcHealthcareServiceRepository implements HealthcareServiceRepository {

    private JdbcSupport jdbcSupport;

    public JdbcHealthcareServiceRepository(DataSource dataSource) {
        this.jdbcSupport = new JdbcSupport(dataSource);
    }

    @Override
    public void save(HealthcareService organization) {
        jdbcSupport.insertInto("organizations")
            .value("her_number", organization.getId())
            .value("display", organization.getDisplay())
            .value("municipality_code", organization.getMunicipalityCode())
            .value("business_type", organization.getBusinessType())
            .value("updated_at", organization.getUpdatedAt())
            .executeUpdate();
    }

    @Override
    public void update(HealthcareService organization) {
        jdbcSupport.executeUpdate("delete from organizations where her_number = ?",
                Arrays.asList(organization.getId()));
        save(organization);
    }

    @Override
    public List<HealthcareService> listPharmacies() {
        return jdbcSupport.queryForList("select * from organizations where business_type = ? order by display",
                Arrays.asList("108"), this::readOrganization);
    }

    private HealthcareService readOrganization(ResultSet rs) throws SQLException {
        HealthcareService organization = new HealthcareService();
        organization.setDisplay(rs.getString("display"));
        organization.setId(rs.getString("her_number"));
        organization.setMunicipalityCode(rs.getString("municipality_code"));
        organization.setBusinessType(rs.getString("business_type"));
        organization.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return organization;
    }


    @Override
    public void refresh(String filename) {
        ArHealthcareServiceImporter importer = new ArHealthcareServiceImporter(this, jdbcSupport);
        importer.refresh(filename);
    }

    public HealthcareService getOrganization(String herId) {
        return jdbcSupport.retrieveSingle("select * from organizations where her_number = ?",
                Arrays.asList(herId), this::readOrganization).get();
    }

}
