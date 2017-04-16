package no.pharmacy.practitioner;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
                + " order by p.first_name, p.last_name limit ?",
                Arrays.asList("LE", 100000), this::read);
    }

    private PersonReference read(ResultSet rs) throws SQLException {
        return new PersonReference(rs.getString("hpr_number"),
                rs.getString("first_name") + " " + rs.getString("last_name"));
    }

    @Override
    public void refresh(URL url) throws IOException {
        HprPractitionerImporter importer = new HprPractitionerImporter(this, jdbcSupport);
        importer.refresh(url);
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

    void update(Practitioner practitioner) {
        jdbcSupport.executeUpdate("delete from practitioner_authorizations where hpr_number = ?",
                Arrays.asList(practitioner.getIdentifier()));

        jdbcSupport.update("practitioners")
            .where("hpr_number", practitioner.getIdentifier())
            .set("encrypted_national_id", jdbcSupport.encrypt(practitioner.getNationalId()))
            .set("first_name", practitioner.getFirstName())
            .set("last_name", practitioner.getLastName())
            .set("date_of_birth", practitioner.getDateOfBirth())
            .set("updated_at", practitioner.getUpdatedAt())
            .executeUpdate();
    }

    void save(Practitioner practitioner) {
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

    public long lastImportTime(URL source) {
        return jdbcSupport.retrieveSingle(
                "select last_import_time from Last_import_time where import_name = ? and source = ?",
                Arrays.asList("practitioners", source),
                rs -> rs.getTimestamp(1).getTime()).orElse(0L);
    }

    public void updateLastImportTime(long lastImportTime, URL source) {
        jdbcSupport.executeUpdate("delete from Last_import_time where import_name = ? and source = ?",
                Arrays.asList("practitioners", source));

        jdbcSupport.insertInto("Last_import_time")
            .value("import_name", "practitioners")
            .value("source", source)
            .value("last_import_time", new Timestamp(lastImportTime))
            .executeUpdate();
    }

}
