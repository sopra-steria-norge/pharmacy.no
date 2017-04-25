package no.pharmacy.patient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.sql.DataSource;

import no.pharmacy.core.PersonReference;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;
import no.pharmacy.infrastructure.jdbc.SelectBuilder;

public class JdbcPatientRepository implements PatientRepository {

    private final JdbcSupport table;
    private final PersonGateway personGateway;

    public JdbcPatientRepository(DataSource dataSource, PersonGateway personGateway, SecretKey secretKey) {
        this.personGateway = personGateway;
        this.table = new JdbcSupport(dataSource);
        this.table.setSecretKey(secretKey);
    }

    @Override
    public List<PersonReference> queryPatient(PersonQuery personQuery) {
        personQuery.validateQuery();
        SelectBuilder query = table.selectStarFrom("patients")
                .where("encrypted_national_id = ?", table.encrypt(personQuery.getNationalId()))
                .where("first_name = ?", personQuery.getFirstName())
                .where("last_name = ?", personQuery.getLastName());
        return query.list(this::readReference);
    }

    @Override
    public PersonReference findPatient(String patientId) {
        return table.retrieveSingle("select * from patients where id = ?", Arrays.asList(patientId), rs -> {
            return readReference(rs);
        }).get();
    }

    @Override
    public PersonReference findPatientByNationalId(String nationalId) {
        return table.retrieveSingle("select * from patients where encrypted_national_id = ?",
                                    Arrays.asList(encrypt(nationalId)),
                                    this::readReference)
                .orElseGet(() -> savePatient(nationalId, lookupName(nationalId)));
    }

    private PersonReference savePatient(String nationalId, PersonReference person) {
        UUID id = UUID.randomUUID();
        table.insertInto("patients")
            .value("id", id.toString())
            .value("encrypted_national_id", encrypt(nationalId))
            .value("first_name", person.getFirstName())
            .value("last_name", person.getLastName())
            .executeUpdate();
        return new PersonReference(id.toString(), person.getFirstName(), person.getLastName());
    }

    private PersonReference readReference(ResultSet rs) throws SQLException {
        return new PersonReference(rs.getString("id"), rs.getString("first_name"), rs.getString("last_name"));
    }

    private PersonReference lookupName(String nationalId) {
        return personGateway.nameByNationalId(nationalId);
    }

    PersonReference savePatient(String nationalId, String firstName, String lastName) {
        UUID id = UUID.randomUUID();
        table.insertInto("patients")
            .value("id", id.toString())
            .value("encrypted_national_id", encrypt(nationalId))
            .value("first_name", firstName)
            .value("last_name", lastName)
            .executeUpdate();

        return new PersonReference(id.toString(), firstName, lastName);
    }

    @Override
    public String lookupPatientNationalId(PersonReference patient) {
        // TODO: Logging!
        return table.retrieveSingle("select encrypted_national_id from patients where id = ?",
                Arrays.asList(patient.getReference()),
                rs -> decrypt(rs.getString(1))).get();
    }

    private String decrypt(String cryptoText) {
        return table.decrypt(cryptoText);
    }

    private String encrypt(String plainText) {
        return table.encrypt(plainText);
    }

}
