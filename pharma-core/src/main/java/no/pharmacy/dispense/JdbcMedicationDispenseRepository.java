package no.pharmacy.dispense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import no.pharmacy.core.Money;
import no.pharmacy.core.Reference;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;
import no.pharmacy.medication.MedicationRepository;

public class JdbcMedicationDispenseRepository extends JdbcSupport implements MedicationDispenseRepository {

    private MedicationRepository medicationRepository;

    public JdbcMedicationDispenseRepository(DataSource dataSource, MedicationRepository medicationRepository) {
        super(dataSource);
        this.medicationRepository = medicationRepository;
    }

    @Override
    public List<DispenseOrder> listReadyForPharmacist() {
        return queryForList("select * from dispense_orders where dispensed = ?", Arrays.asList(false),
                this::read);
        // TODO Return DispenseOrderSummary
    }

    @Override
    public void saveDispenseOrder(DispenseOrder order) {
        order.setIdentifier(UUID.randomUUID().toString());
        insertInto("dispense_orders")
            .value("id", order.getIdentifier())
            .value("customer_signature", order.getCustomerSignature())
            .value("dispensed", order.isDispensed())
            .executeUpdate();

        for (MedicationOrder medicationOrder : order.getMedicationOrders()) {
            long id = insertInto("medication_orders")
                .value("dispense_order_id", order.getIdentifier())
                .value("prescriber_id", medicationOrder.getPrescriber())
                .value("prescription_id", medicationOrder.getPrescriptionId())
                .value("date_written", medicationOrder.getDateWritten())
                .value("medication_id", medicationOrder.getMedication().getProductId())
                .value("dosage_text", medicationOrder.getDosageText())
                .executeInsert();
            medicationOrder.setId(id);
        }

        for (MedicationDispense dispense : order.getMedicationDispenses()) {
            long id = insertInto("medication_dispenses")
                .value("dispense_order_id", order.getIdentifier())
                .value("price", dispense.getPrice())
                .value("printed_dosage_text", dispense.getPrintedDosageText())
                .value("authorizing_prescription_id", dispense.getAuthorizingPrescription().getId())
                .value("confirmed_by_pharmacist", dispense.isConfirmedByPharmacist())
                .value("packaging_controlled", dispense.isPackagingControlled())
                .executeInsert();
            dispense.setId(id);
        }
    }

    @Override
    public void update(DispenseOrder order) {
        update("dispense_orders")
            .where("id", order.getIdentifier())
            .set("customer_signature", order.getCustomerSignature())
            .set("dispensed", order.isDispensed())
            .executeUpdate();

        for (MedicationDispense dispense : order.getMedicationDispenses()) {
            update(dispense);
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
        result.setCustomerSignature(rs.getString("customer_signature"));
        result.setDispensed(rs.getBoolean("dispensed"));

        result.getMedicationOrders().addAll(findMedicationOrders(result.getIdentifier()));
        result.getMedicationDispenses().addAll(findMedicationDispenses(result.getIdentifier(),
                result.getMedicationOrders()));

        return result;
    }

    private List<MedicationDispense> findMedicationDispenses(String identifier, List<MedicationOrder> prescriptions) {
        return queryForResultSet("select * from medication_dispenses d left outer join medication_dispense_actions a on d.id = a.dispense_id where dispense_order_id = ? order by id",
                Arrays.asList(identifier),
                rs -> readMedicationDispense(rs, prescriptions));
    }

    private List<MedicationDispense> readMedicationDispense(ResultSet rs, List<MedicationOrder> prescriptions) throws SQLException {
        List<MedicationDispense> result = new ArrayList<>();
        long previousId = -1;

        while (rs.next()) {
            long prescriptionId = rs.getLong("authorizing_prescription_id");
            if (prescriptionId != previousId) {
                MedicationOrder medicationOrder = prescriptions.stream().filter(p -> p.getId() == prescriptionId).findFirst().get();

                MedicationDispense dispense = new MedicationDispense(medicationOrder);
                dispense.setId(rs.getLong("id"));
                dispense.setPrice(Money.from(rs.getBigDecimal("price")));
                dispense.setPrintedDosageText(rs.getString("printed_dosage_text"));
                dispense.setMedication(medicationRepository.findByProductId(rs.getString("medication_id"))
                        .orElse(null));
                dispense.setConfirmedByPharmacist(rs.getBoolean("confirmed_by_pharmacist"));
                dispense.setPackagingControlled(rs.getBoolean("packaging_controlled"));
                result.add(dispense);

                previousId = prescriptionId;
            }
            if (rs.getString("interaction_id") != null) {
                result.get(result.size()-1).addMedicationDispenseAction(readMedicationDispenseAction(rs));
            }
        }
        return result;
    }

    private MedicationDispenseAction readMedicationDispenseAction(ResultSet rs) throws SQLException {
        return new MedicationDispenseAction(
                new MedicationOrderWarning(
                        rs.getLong("interacting_dispense_id"), rs.getString("interacting_dispense_display"),
                        medicationRepository.getInteraction(rs.getString("interaction_id"))),
                rs.getString("warning_remark"), rs.getString("warning_action"));
    }

    private MedicationOrder readMedicationOrder(ResultSet rs) throws SQLException {
        MedicationOrder medicationOrder = new MedicationOrder();
        // TODO: Join in doctor
        medicationOrder.setId(rs.getLong("id"));
        medicationOrder.setPrescriber(new Reference(rs.getString("prescriber_id"), "Random J Doctor"));
        medicationOrder.setDateWritten(toLocalDate(rs.getDate("date_written")));
        medicationOrder.setDosageText(rs.getString("dosage_text"));
        medicationOrder.setPrescriptionId(rs.getString("prescription_id"));
        medicationOrder.setMedication(medicationRepository.findByProductId(rs.getString("medication_id")).get());
        medicationOrder.setAlternatives(medicationRepository.listAlternatives(medicationOrder.getMedication()));

        return medicationOrder;
    }

    private List<MedicationOrder> findMedicationOrders(String identifier) {
        return queryForList("select * from medication_orders where dispense_order_id = ?",
                Arrays.asList(identifier), this::readMedicationOrder);
    }

    @Override
    public void update(MedicationDispense dispense) {
        update("medication_dispenses")
            .where("id", dispense.getId())
            .set("price", dispense.getPrice())
            .set("printed_dosage_text", dispense.getPrintedDosageText())
            .set("medication_id", dispense.getMedicationId())
            .set("confirmed_by_pharmacist", dispense.isConfirmedByPharmacist())
            .set("packaging_controlled", dispense.isPackagingControlled())
            .executeUpdate();

        executeUpdate("delete from medication_dispense_actions where dispense_id = ?", Arrays.asList(dispense.getId()));
        for (MedicationDispenseAction action : dispense.getWarningActions()) {
            insertInto("medication_dispense_actions")
                .value("dispense_id", dispense.getId())
                .value("interaction_id", action.getWarningCode())
                .value("interacting_dispense_display", action.getWarning().displayInteractingDispense())
                .value("interacting_dispense_id", action.getWarning().getInteractingDispenseId())
                .value("warning_action", action.getAction())
                .value("warning_remark", action.getRemark())
                .executeUpdate();
        }
    }
}
