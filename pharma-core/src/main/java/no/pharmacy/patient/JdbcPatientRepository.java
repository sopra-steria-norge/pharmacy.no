package no.pharmacy.patient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.sql.DataSource;

import no.pharmacy.core.Reference;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class JdbcPatientRepository implements PatientRepository {

    private final JdbcSupport table;
    private final PersonGateway personGateway;

    public JdbcPatientRepository(DataSource dataSource, PersonGateway personGateway, SecretKey secretKey) {
        this.personGateway = personGateway;
        this.table = new JdbcSupport(dataSource);
        this.table.setSecretKey(secretKey);
    }

    @Override
    public Reference findPatient(String patientId) {
        return table.retrieveSingle("select * from patients where id = ?", Arrays.asList(patientId), rs -> {
            return readReference(rs);
        }).get();
    }

    @Override
    public Reference findPatientByNationalId(String nationalId) {
        return table.retrieveSingle("select * from patients where encrypted_national_id = ?",
                                    Arrays.asList(encrypt(nationalId)),
                                    this::readReference)
                .orElseGet(() -> savePatient(nationalId, lookupName(nationalId)));
    }

    private Reference readReference(ResultSet rs) throws SQLException {
        return new Reference(rs.getString("id"), rs.getString("name"));
    }

    private String lookupName(String nationalId) {
        return personGateway.nameByNationalId(nationalId);
    }

    Reference savePatient(String nationalId, String name) {
        UUID id = UUID.randomUUID();
        table.insertInto("patients")
            .value("id", id.toString())
            .value("encrypted_national_id", encrypt(nationalId))
            .value("name", name)
            .executeUpdate();

        return new Reference(id.toString(), name);
    }

    String lookupPatientNationalId(Reference patient) {
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
