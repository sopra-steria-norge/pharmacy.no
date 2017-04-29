package no.pharmacy.test;

import java.util.List;

import no.pharmacy.core.PersonReference;
import no.pharmacy.dispense.MedicationOrder;

public interface PrescriptionSimulator {

    MedicationOrder addPrescription(String nationalId, String medicationProductId, PersonReference prescriber);

    List<MessageLogEntry> getMessageLog();

}
