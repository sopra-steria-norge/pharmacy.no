package no.pharmacy.web.server;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;
import no.pharmacy.core.MessageGateway;
import no.pharmacy.dispense.JdbcMedicationDispenseRepository;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.medicationorder.PrescriptionGateway;
import no.pharmacy.medicationorder.RFPrescriptionGateway;
import no.pharmacy.organization.HealthcareServiceRepository;
import no.pharmacy.patient.PatientRepository;
import no.pharmacy.practitioner.PractitionerRepository;

public class PharmaApplicationContext {

    @Getter
    private final MedicationRepository medicationRepository;

    @Getter
    private MedicationDispenseRepository repository;

    @Getter @Setter
    private PatientRepository patientRepository;

    @Getter @Setter
    private HealthcareServiceRepository healthcareServiceRepository;

    @Getter @Setter
    private PractitionerRepository practitionerRepository;

    @Setter
    private MessageGateway rfMessageGateway;

    public PharmaApplicationContext(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public void setPharmacistDataSource(DataSource dataSource) {
        repository = new JdbcMedicationDispenseRepository(dataSource, medicationRepository, healthcareServiceRepository);
    }

    public PrescriptionGateway getPrescriptionGateway() {
        return new RFPrescriptionGateway(rfMessageGateway, medicationRepository, patientRepository);
    }

}
