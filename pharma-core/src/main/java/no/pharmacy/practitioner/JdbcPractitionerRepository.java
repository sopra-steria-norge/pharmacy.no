package no.pharmacy.practitioner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.core.PersonReference;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class JdbcPractitionerRepository implements PractitionerRepository {

    private static final Logger logger = LoggerFactory.getLogger(JdbcPractitionerRepository.class);

    private JdbcSupport jdbcSupport;

    public JdbcPractitionerRepository(DataSource dataSource) {
        jdbcSupport = new JdbcSupport(dataSource);
    }

    @Override
    public List<PersonReference> listDoctors() {
        return jdbcSupport.queryForList(
                "select * from practitioners p"
                + " where hpr_number in (select hpr_number from Practitioner_authorizations where authorization_code = ?)",
                Arrays.asList("LE"), this::read);
    }

    private PersonReference read(ResultSet rs) throws SQLException {
        return new PersonReference(rs.getString("hpr_number"), rs.getString("name"));
    }

    @Override
    public void refresh(String hprLocation) {
        HprPractitionerImporter importer = new HprPractitionerImporter(this, jdbcSupport);
        importer.refresh(hprLocation);
    }

    public List<PractionerAuthorization> getAuthorizations(String hprNumber) {
        return jdbcSupport.queryForList(
                "select * from practitioner_authorizations where hpr_number = ?",
                Arrays.asList(hprNumber),
                rs -> PractionerAuthorization.getValue(rs.getString("authorization_code")));
    }

    void save(Practitioner practitioner) {
        // TODO: Keep firstname, lastname. Include encrypted_national_id, include date_of_birth, last_update_in_hpr
        jdbcSupport.insertInto("practitioners")
            .value("hpr_number", practitioner.getIdentifier())
            .value("name", practitioner.getName())
            .executeUpdate();
    }

    void saveAuthorization(Long hprNumber, String authorization) {
        jdbcSupport.insertInto("Practitioner_authorizations")
            .value("hpr_number", hprNumber)
            .value("authorization_code", authorization)
            .executeUpdate();
    }
}
