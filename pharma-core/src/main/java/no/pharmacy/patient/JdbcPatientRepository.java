package no.pharmacy.patient;

import java.util.Arrays;
import java.util.UUID;

import javax.sql.DataSource;

import no.pharmacy.core.Reference;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class JdbcPatientRepository implements PatientRepository {

    private JdbcSupport table;
    private PersonGateway personGateway;

    public JdbcPatientRepository(DataSource dataSource, PersonGateway personGateway) {
        this.personGateway = personGateway;
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
        return table.retrieveSingle("select * from patients where encrypted_national_id = ?", Arrays.asList(nationalId), rs ->
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
            .value("encrypted_national_id", nationalId)
            .value("name", name)
            .executeUpdate();

        return new Reference(id.toString(), name);
    }

}
