package no.pharmacy.patient;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.sql.DataSource;

import no.pharmacy.core.Reference;
import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class JdbcPatientRepository implements PatientRepository {

    private final JdbcSupport table;
    private final PersonGateway personGateway;
    private final SecretKey secretKey;

    public JdbcPatientRepository(DataSource dataSource, PersonGateway personGateway, SecretKey secretKey) {
        this.personGateway = personGateway;
        this.secretKey = secretKey;
        this.table = new JdbcSupport(dataSource);
    }

    @Override
    public Reference findPatient(String patientId) {
        return table.retrieveSingle("select * from patients where id = ?", Arrays.asList(patientId), rs -> {
            return new Reference(rs.getString("id"), rs.getString("name"));
        }).get();
    }

    @Override
    public Reference findPatientByNationalId(String nationalId) {
        return table.retrieveSingle("select * from patients where encrypted_national_id = ?", Arrays.asList(encrypt(nationalId)), rs ->
            new Reference(rs.getString("id"), rs.getString("name"))
        ).orElseGet(() ->
            savePatient(nationalId, lookupName(nationalId))
        );
    }

    private String lookupName(String nationalId) {
        return personGateway.nameByNationalId(nationalId);
    }

    public Reference savePatient(String nationalId, String name) {
        UUID id = UUID.randomUUID();
        table.insertInto("patients")
            .value("id", id.toString())
            .value("encrypted_national_id", encrypt(nationalId))
            .value("name", name)
            .executeUpdate();

        return new Reference(id.toString(), name);
    }

    public String lookupPatientNationalId(Reference patient) {
        // TODO: Logging!
        return table.retrieveSingle("select encrypted_national_id from patients where id = ?",
                Arrays.asList(patient.getReference()),
                rs -> decrypt(rs.getString(1))).get();
    }

    private String decrypt(String cryptoText) {
        try {
            Cipher encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(encryptCipher.doFinal(Base64.getDecoder().decode(cryptoText.getBytes())));
        } catch (GeneralSecurityException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    String encrypt(String plainText) {
        try {
            Cipher encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(encryptCipher.doFinal(plainText.getBytes()));
        } catch (GeneralSecurityException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

}
