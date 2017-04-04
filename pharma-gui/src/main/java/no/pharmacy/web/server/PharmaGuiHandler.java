package no.pharmacy.web.server;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import lombok.Setter;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.medicationorder.PrescriptionGateway;
import no.pharmacy.organization.HealthcareServiceRepository;
import no.pharmacy.patient.PatientRepository;
import no.pharmacy.web.dispense.DispenseOrderController;
import no.pharmacy.web.dispense.PharmacistController;
import no.pharmacy.web.dispense.PrescriptionsController;

public class PharmaGuiHandler {

    @Setter
    private PrescriptionGateway prescriptionGateway;

    @Setter
    private MedicationRepository medicationRepository;

    @Setter
    private MedicationDispenseRepository repository;

    @Setter
    private PatientRepository patientRepository;

    @Setter
    private HealthcareServiceRepository healthcareServiceRepository;

    private WebAppContext handler = new WebAppContext(null, "/");

    public Handler createHandler() {
        handler.setBaseResource(Resource.newClassPathResource("/pharma-webapp"));

        addServlet(new PrescriptionsController(prescriptionGateway, repository, patientRepository), "/prescriptions/");
        addServlet(new DispenseOrderController(prescriptionGateway, repository, medicationRepository), "/dispenseOrder/*");
        addServlet(new PharmacistController(repository), "/pharmacist/*");
        addServlet(new SelectPharmacyController(healthcareServiceRepository), "/selectPharmacy/*");

        return handler;
    }


    private void addServlet(HttpServlet servlet, String pathSpec) {
        handler.addServlet(new ServletHolder(servlet), pathSpec);
    }


}
