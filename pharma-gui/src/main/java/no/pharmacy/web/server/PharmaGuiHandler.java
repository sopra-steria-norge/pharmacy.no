package no.pharmacy.web.server;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import lombok.Setter;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.medicationorder.PrescriptionGateway;
import no.pharmacy.organization.HealthcareServiceRepository;
import no.pharmacy.patient.PatientRepository;
import no.pharmacy.practitioner.PractitionerRepository;
import no.pharmacy.web.dispense.DispenseOrderController;
import no.pharmacy.web.dispense.PharmacistController;
import no.pharmacy.web.dispense.PrescriptionsController;
import no.pharmacy.web.infrastructure.auth.AuthenticationConfiguration;
import no.pharmacy.web.infrastructure.auth.AuthenticationFilter;

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

    @Setter
    private PractitionerRepository practitionerRepository;

    private WebAppContext handler = new WebAppContext(null, "/");

    @SuppressWarnings("resource")
    public Handler createHandler() throws IOException {
        handler.setBaseResource(Resource.newClassPathResource("/pharma-webapp"));

        Resource resource = Resource.newResource("src/main/resources/pharma-webapp");
        if (resource.isDirectory()) {
            handler.setBaseResource(resource);
            // Avoid locking files on disk
            handler.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        }

        addServlet(new PrescriptionsController(prescriptionGateway, repository, patientRepository), "/prescriptions/");
        addServlet(new DispenseOrderController(prescriptionGateway, repository, medicationRepository), "/dispenseOrder/*");
        addServlet(new PharmacistController(repository), "/pharmacist/*");
        addServlet(new SelectPharmacyController(healthcareServiceRepository), "/selectPharmacy/*");
        addServlet(new PharmacyGuiController(
                prescriptionGateway,
                repository,
                patientRepository,
                medicationRepository,
                healthcareServiceRepository,
                practitionerRepository), "/pharmacies/api/*");

        handler.addFilter(new FilterHolder(new AuthenticationFilter(new AuthenticationConfiguration())), "/pharmacies/*", EnumSet.of(DispatcherType.REQUEST));

        return handler;
    }


    private void addServlet(HttpServlet servlet, String pathSpec) {
        handler.addServlet(new ServletHolder(servlet), pathSpec);
    }
}
