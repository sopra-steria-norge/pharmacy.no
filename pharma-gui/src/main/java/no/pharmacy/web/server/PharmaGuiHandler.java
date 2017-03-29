package no.pharmacy.web.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import lombok.Setter;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.medicationorder.PrescriptionGateway;
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


    public Handler createHandler() {
        WebAppContext handler = new WebAppContext(null, "/");
        handler.setBaseResource(Resource.newClassPathResource("/pharma-webapp"));

        handler.addServlet(new ServletHolder(new PrescriptionsController(prescriptionGateway, repository, patientRepository)), "/prescriptions/");
        handler.addServlet(new ServletHolder(new DispenseOrderController(prescriptionGateway, repository, medicationRepository)), "/dispenseOrder/*");
        handler.addServlet(new ServletHolder(new PharmacistController(repository)), "/pharmacist/*");

        return handler;
    }


}
