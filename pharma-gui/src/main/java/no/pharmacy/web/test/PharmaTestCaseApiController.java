package no.pharmacy.web.test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.practitioner.Practitioner;
import no.pharmacy.practitioner.PractitionerRepository;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.PrescriptionSimulator;
import no.pharmacy.web.server.PharmaApplicationContext;

public class PharmaTestCaseApiController extends HttpServlet {

    private PrescriptionSimulator reseptFormidler;
    private MedicationRepository medicationRepository;
    private PractitionerRepository practitionerRepository;
    private PharmaTestData testData = new PharmaTestData();

    public PharmaTestCaseApiController(
            PrescriptionSimulator reseptFormidler,
            MedicationRepository medicationRepository,
            PractitionerRepository practitionerRepository) {
        this.reseptFormidler = reseptFormidler;
        this.medicationRepository = medicationRepository;
        this.practitionerRepository = practitionerRepository;
    }

    public PharmaTestCaseApiController(PrescriptionSimulator reseptFormidler, PharmaApplicationContext context) {
        this(reseptFormidler, context.getMedicationRepository(), context.getPractitionerRepository());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        Map<Pattern, Function<HttpServletRequest, JsonNode>> router = new LinkedHashMap<>();
        router.put(Pattern.compile("/patients"), request -> {
            Random random = new Random(324523L);
            return JsonArray.map(testData.unusedNationalIds(random, 1000),
                    nationalId -> new JsonObject()
                        .put("nationalId", nationalId)
                        .put("firstName", PharmaTestData.sampleFirstName(random))
                        .put("lastName", PharmaTestData.sampleLastName(random)));
        });
        router.put(Pattern.compile("/practitioners"), request -> {
            return JsonArray.map(practitionerRepository.listDoctors(),
                    practitioner -> new JsonObject()
                        .put("id", practitioner.getReference())
                        .put("name", practitioner.getDisplay()));
        });
        router.put(Pattern.compile("/medications"), request -> {
            return JsonArray.map(medicationRepository.list(0, 100000),
                    m -> new JsonObject()
                        .put("productId", m.getProductId())
                        .put("display", m.getDisplay())
                        .put("atc", m.getSubstance()));
        });

        for (Pattern pattern : router.keySet()) {
            if (pattern.matcher(pathInfo).matches()) {
                JsonNode response = router.get(pattern).apply(req);
                resp.setContentType("application/json");
                response.toJson(resp.getWriter());
                return;
            }
        }


        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject prescriptionCollection = JsonParser.parseToObject(req.getParameter("JSON"));
        JsonObject patient = prescriptionCollection.requiredObject("patient");

        prescriptionCollection.requiredArray("prescriptions").objectStream()
            .forEach(prescription -> {
                Practitioner prescriber = practitionerRepository.getPractitioner(prescription.requiredObject("prescriber").requiredString("id")).get();

                reseptFormidler.addPrescription(
                        patient.requiredString("nationalId"),
                        prescription.requiredString("medication"),
                        prescriber.getReference());

            });


        resp.sendRedirect(req.getContextPath() + "?patientNationalId=" + patient.requiredString("nationalId"));
    }

}
