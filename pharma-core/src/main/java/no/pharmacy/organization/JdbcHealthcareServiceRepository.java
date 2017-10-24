package no.pharmacy.organization;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class JdbcHealthcareServiceRepository implements HealthcareServiceRepository {

    public static final URL SEED_URL = JdbcHealthcareServiceRepository.class.getResource("/seed/AR-mini.xml.gz");
    private JdbcSupport jdbcSupport;

    public JdbcHealthcareServiceRepository(DataSource dataSource, URL seedUrl) {
        this.jdbcSupport = new JdbcSupport(dataSource);
        refresh(seedUrl);
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
    public HealthcareService retrieve(String herNumber) {
        if (herNumber == null) {
            return null;
        }
        return jdbcSupport.retrieveSingle(
                "select * from organizations where her_number = ?",
                Arrays.asList(herNumber), this::readOrganization).get();
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


    void refresh(URL path) {
        ArHealthcareServiceImporter importer = new ArHealthcareServiceImporter(this, jdbcSupport);
        importer.refresh(path);
    }

    @Override
    public long lastImportTime(URL url) {
        return jdbcSupport.retrieveSingle(
                "select last_import_time from Last_import_time where import_name = ? and source = ?",
                Arrays.asList("organizations", url.toString()),
                rs -> rs.getTimestamp(1).getTime()).orElse(0L);
    }

    @Override
    public void updateLastImportTime(long lastImportTime, URL source) {
        jdbcSupport.executeUpdate("delete from Last_import_time where import_name = ? and source = ?",
                Arrays.asList("organizations", source.toString()));

        jdbcSupport.insertInto("Last_import_time")
            .value("import_name", "organizations")
            .value("source", source.toString())
            .value("last_import_time", new Timestamp(lastImportTime))
            .executeUpdate();
    }
}
