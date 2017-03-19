package no.pharmacy.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import no.pharmacy.core.Money;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.order.DispenseOrder;
import no.pharmacy.order.MedicationDispenseRepository;
import no.pharmacy.order.MedicationOrder;
import no.pharmacy.order.Reference;

public class JdbcMedicationDispenseRepository extends JdbcSupport implements MedicationDispenseRepository {

    private MedicationRepository medicationRepository;

    public JdbcMedicationDispenseRepository(DataSource dataSource, MedicationRepository medicationRepository) {
        super(dataSource);
        this.medicationRepository = medicationRepository;
    }

    @Override
    public void saveDispenseOrder(DispenseOrder order) {
        order.setIdentifier(UUID.randomUUID().toString());
        insertInto("dispense_orders")
            .value("id", order.getIdentifier())
            .executeUpdate();

        for (MedicationOrder medicationOrder : order.getMedicationOrders()) {
            long id = insertInto("medication_orders")
                .value("dispense_order_id", order.getIdentifier())
                .value("prescriber_id", medicationOrder.getPrescriber())
                .value("date_written", medicationOrder.getDateWritten())
                .value("medication_id", medicationOrder.getMedication().getProductId())
                .executeInsert();
            medicationOrder.setId(id);
        }

        for (MedicationDispense medicationDispense : order.getMedicationDispenseList()) {
            long id = insertInto("medication_dispenses")
                .value("dispense_order_id", order.getIdentifier())
                .value("authorizing_prescription_id", medicationDispense.getAuthorizingPrescription().getId())
                .executeInsert();
            medicationDispense.setId(id);
        }

    }

    @Override
    public DispenseOrder getDispenseOrderById(String id) {
        return retrieveSingle("select * from dispense_orders where id = ?",
                Arrays.asList(id),
                this::read).get();
    }

    private DispenseOrder read(ResultSet rs) throws SQLException {
        DispenseOrder result = new DispenseOrder();
        result.setIdentifier(rs.getString("id"));

        result.getMedicationOrders().addAll(findMedicationOrders(result.getIdentifier()));
        result.getMedicationDispenseList().addAll(findMedicationDispenses(result.getIdentifier(),
                result.getMedicationOrders()));

        return result;
    }

    private List<MedicationDispense> findMedicationDispenses(String identifier, List<MedicationOrder> prescriptions) {
        return queryForList("select * from medication_dispenses where dispense_order_id = ?",
                Arrays.asList(identifier),
                rs -> readMedicationDispense(rs, prescriptions));
    }

    private MedicationDispense readMedicationDispense(ResultSet rs, List<MedicationOrder> prescriptions) throws SQLException {
        long prescriptionId = rs.getLong("authorizing_prescription_id");
        MedicationOrder medicationOrder = prescriptions.stream().filter(p -> p.getId() == prescriptionId).findFirst().get();

        MedicationDispense dispense = new MedicationDispense(medicationOrder);
        dispense.setId(rs.getLong("id"));
        dispense.setPrice(Money.from(rs.getBigDecimal("price")));
        dispense.setMedication(medicationRepository.findByProductId(rs.getString("medication_id"))
                .orElse(null));
        return dispense;
    }

    private MedicationOrder readMedicationOrder(ResultSet rs) throws SQLException {
        MedicationOrder medicationOrder = new MedicationOrder();
        // TODO: Join in doctor
        medicationOrder.setId(rs.getLong("id"));
        medicationOrder.setPrescriber(new Reference(rs.getString("prescriber_id"), "Random J Doctor"));
        medicationOrder.setDateWritten(toLocalDate(rs.getDate("date_written")));
        medicationOrder.setMedication(medicationRepository.findByProductId(rs.getString("medication_id")).get());
        medicationOrder.setAlternatives(medicationRepository.listAlternatives(medicationOrder.getMedication()));

        return medicationOrder;
    }

    private List<MedicationOrder> findMedicationOrders(String identifier) {
        return queryForList("select * from medication_orders where dispense_order_id = ?",
                Arrays.asList(identifier), this::readMedicationOrder);
    }

    @Override
    public void update(MedicationDispense prescription) {
        update("medication_dispenses")
            .where("id", prescription.getId())
            .set("price", prescription.getPrice())
            .set("medication_id", prescription.getMedication().getProductId())
            .executeUpdate();
    }
}
