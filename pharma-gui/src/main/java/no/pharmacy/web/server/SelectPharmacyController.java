package no.pharmacy.web.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.organization.HealthcareService;
import no.pharmacy.organization.HealthcareServiceRepository;

public class SelectPharmacyController extends HttpServlet {

    private HealthcareServiceRepository healthcareServiceRepository;

    public SelectPharmacyController(HealthcareServiceRepository healthcareServiceRepository) {
        this.healthcareServiceRepository = healthcareServiceRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Document doc = Xml.readResource("/pharma-webapp/pharmacy/select.html.template");

        Element results = doc.find("...", "#healthcareServices").first();

        for (HealthcareService service : healthcareServiceRepository.listPharmacies()) {
            results.add(Xml.el("div",
                    Xml.el("label",
                        Xml.el("input")
                            .type("radio").name("healthcareServiceId").val(service.getId()),
                        Xml.el("span", service.getDisplay()))));
        }

        resp.setContentType("text/html");
        doc.writeTo(resp.getWriter());
    }

}
