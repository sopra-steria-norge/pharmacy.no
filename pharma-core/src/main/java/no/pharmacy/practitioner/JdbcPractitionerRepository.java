package no.pharmacy.practitioner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.sql.DataSource;

import no.pharmacy.core.PersonReference;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class JdbcPractitionerRepository implements PractitionerRepository {

    private JdbcSupport jdbcSupport;

    public JdbcPractitionerRepository(DataSource dataSource, SecretKey secretKey) {
        jdbcSupport = new JdbcSupport(dataSource);
        jdbcSupport.setSecretKey(secretKey);
    }

    @Override
    public List<PersonReference> listDoctors() {
        return jdbcSupport.queryForList(
                "select * from practitioners p"
                + " where hpr_number in (select hpr_number from Practitioner_authorizations where authorization_code = ?)"
                + " order by hpr_number limit ?",
                Arrays.asList("LE", 20), this::read);
    }

    private PersonReference read(ResultSet rs) throws SQLException {
        return new PersonReference(rs.getString("hpr_number"),
                rs.getString("first_name") + " " + rs.getString("last_name"));
    }

    @Override
    public void refresh(String hprLocation) {
        HprPractitionerImporter importer = new HprPractitionerImporter(this, jdbcSupport);
        importer.refresh(hprLocation);
    }

    @Override
    public Optional<Practitioner> getPractitioner(String hprNumber) {
        return jdbcSupport.retrieveSingle(
                "select * from practitioners where hpr_number = ?",
                Arrays.asList(hprNumber),
                rs -> readWithAuthorizations(rs));
    }

    private Practitioner readWithAuthorizations(ResultSet rs) throws SQLException {
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier(rs.getLong("hpr_number"));
        practitioner.setFirstName(rs.getString("first_name"));
        practitioner.setLastName(rs.getString("last_name"));
        practitioner.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        practitioner.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        practitioner.setNationalId(jdbcSupport.decrypt(rs.getString("encrypted_national_id")));
        practitioner.getAuthorizations().addAll(getAuthorizations(practitioner.getIdentifier()));
        return practitioner;
    }

    public List<PractionerAuthorization> getAuthorizations(long hprNumber) {
        return jdbcSupport.queryForList(
                "select * from practitioner_authorizations where hpr_number = ?",
                Arrays.asList(hprNumber),
                rs -> PractionerAuthorization.getValue(rs.getString("authorization_code")));
    }

    void save(Practitioner practitioner) {
        jdbcSupport.executeUpdate("delete from practitioner_authorizations where hpr_number = ?",
                Arrays.asList(practitioner.getIdentifier()));
        jdbcSupport.executeUpdate("delete from practitioners where hpr_number = ?",
                Arrays.asList(practitioner.getIdentifier()));

        jdbcSupport.insertInto("practitioners")
            .value("hpr_number", practitioner.getIdentifier())
            .value("encrypted_national_id", jdbcSupport.encrypt(practitioner.getNationalId()))
            .value("first_name", practitioner.getFirstName())
            .value("last_name", practitioner.getLastName())
            .value("date_of_birth", practitioner.getDateOfBirth())
            .value("updated_at", practitioner.getUpdatedAt())
            .executeUpdate();
    }

    void saveAuthorization(Long id, Long hprNumber, String authorization) {
        jdbcSupport.insertInto("Practitioner_authorizations")
            .value("id", id)
            .value("hpr_number", hprNumber)
            .value("authorization_code", authorization)
            .executeUpdate();
    }

}
