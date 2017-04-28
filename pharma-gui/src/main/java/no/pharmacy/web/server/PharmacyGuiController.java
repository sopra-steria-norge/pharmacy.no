package no.pharmacy.web.server;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import no.pharmacy.core.Money;
import no.pharmacy.core.PersonReference;
import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.DispenseOrderService;
import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationDispenseAction;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.medication.Medication;
import no.pharmacy.medication.MedicationRepository;
import no.pharmacy.medicationorder.MedicationOrderSummary;
import no.pharmacy.medicationorder.PrescriptionGateway;
import no.pharmacy.organization.HealthcareServiceRepository;
import no.pharmacy.patient.HealthRecordQuery;
import no.pharmacy.patient.HealthRecordService;
import no.pharmacy.patient.PatientRepository;
import no.pharmacy.patient.PersonQuery;
import no.pharmacy.practitioner.PharmacyPrincipal;
import no.pharmacy.practitioner.Practitioner;
import no.pharmacy.practitioner.PractitionerRepository;
import no.pharmacy.web.infrastructure.auth.AuthenticationConfiguration;
import no.pharmacy.web.infrastructure.auth.OpenIdPrincipal;

// TODO: Store, filter and retrieve HER and HPR numbers
// TODO: The structure of this class should be rethought
//      - each route should be implemented by a separate method to ease navigation
//      - the pattern match shouldn't defined twice where URL fragments are used
//      - doPost has different return modes (redirect, Location,...)
// TODO: The structure should allow for unit tests
public class PharmacyGuiController extends HttpServlet {

    private MedicationDispenseRepository repository;
    private PatientRepository patientRepository;
    private MedicationRepository medicationRepository;
    private HealthcareServiceRepository healthcareServiceRepository;
    private PractitionerRepository practitionerRepository;
    private DispenseOrderService dispenseOrderService;
    private HealthRecordService healthRecordService;

    public PharmacyGuiController(PrescriptionGateway prescriptionGateway, MedicationDispenseRepository repository,
            PatientRepository patientRepository, MedicationRepository medicationRepository,
            HealthcareServiceRepository healthcareServiceRepository, PractitionerRepository practitionerRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.medicationRepository = medicationRepository;
        this.healthcareServiceRepository = healthcareServiceRepository;
        this.practitionerRepository = practitionerRepository;

        dispenseOrderService = new DispenseOrderService(prescriptionGateway, repository, patientRepository);
        healthRecordService = new HealthRecordService(repository, patientRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<Pattern, Function<HttpServletRequest, JsonNode>> router = new LinkedHashMap<>();

        router.put(Pattern.compile("/me"), request -> {
            PharmacyPrincipal principal = createPharmacyPrincipal((OpenIdPrincipal) req.getUserPrincipal());
            return new JsonObject()
                    .put("name", principal.getName())
                    .put("display_name", principal.getDisplayName())
                    .put("hpr_number", principal.getHprNumber())
                    .put("authorizations",
                           JsonArray.fromStringStream(principal.getPractitioner().getAuthorizations().stream().map(e -> e.toString())))
                    .put("organizations", JsonArray.map(principal.getOrganizations(), healthcareService ->
                            new JsonObject()
                                .put("id", healthcareService.getId())
                                    .put("municipality", healthcareService.getMunicipalityCode())
                                    .put("display", healthcareService.getDisplay())));
        });

        router.put(Pattern.compile("/(\\d+)/pharmacistOrders/?"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/pharmacistOrders/?").matcher(req.getPathInfo());
            match.matches();

            List<DispenseOrder> readyForPharmacist = repository.listReadyForPharmacist();

            return JsonArray.map(readyForPharmacist, dispenseOrder -> new JsonObject()
                    .put("patient", new JsonObject()
                            .put("id", dispenseOrder.getPatient().getReference())
                            .put("firstName", dispenseOrder.getPatient().getFirstName())
                            .put("lastName", dispenseOrder.getPatient().getLastName()))
                    .put("pharmacistControlComplete", dispenseOrder.isPharmacistControlComplete())
                    .put("id", dispenseOrder.getIdentifier()));
        });

        router.put(Pattern.compile("/(\\d+)/persons/([-0-9a-f]+)"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/persons/([-0-9a-f]+)").matcher(req.getPathInfo());
            match.matches();

            PersonReference person = patientRepository.findPatient(match.group(2));

            return new JsonObject()
                    .put("id", person.getReference())
                    .put("firstName", person.getFirstName())
                    .put("lastName", person.getLastName())
                    ;
        });

        router.put(Pattern.compile("/\\d+/prescriptionQueries/([-0-9a-f]+)"), request -> {
            Matcher match = Pattern.compile("/\\d+/prescriptionQueries/([-0-9a-f]+)").matcher(req.getPathInfo());
            match.matches();
            UUID queryId = UUID.fromString(match.group(1));
            List<MedicationOrderSummary> listPrescriptions = repository.listPrescriptionsFromQuery(queryId);

            return JsonArray.map(listPrescriptions,
                    prescription -> new JsonObject()
                        .put("queryId", queryId.toString())
                        .put("id", prescription.getPrescriptionId())
                        .put("prescriber", new JsonObject()
                                .put("name", prescription.getPrescriberName()))
                        .put("medicationName", prescription.getMedicationName())
                        .put("dateWritten", prescription.getDateWritten().toString()));
        });

        router.put(Pattern.compile("/\\d+/patientHistoryQuery/([-0-9a-f]+)"), request -> {
            Matcher match = Pattern.compile("/\\d+/patientHistoryQuery/([-0-9a-f]+)").matcher(req.getPathInfo());
            match.matches();
            UUID queryId = UUID.fromString(match.group(1));
            List<DispenseOrder> orders = healthRecordService.listMedicationDispenses(queryId);

            return new JsonObject()
                    .put("patient", new JsonObject().put("firstName", "Dummy").put("lastName", "Dummyson"))
                    .put("orders", JsonArray.map(orders, o -> new JsonObject()
                            .put("id", o.getIdentifier())
                            .put("dispensed", o.isDispensed())
                            .put("dateDispensed", Objects.toString(o.getDateDispensed(), null))
                            .put("patient", new JsonObject()
                                    .put("id", o.getPatient().getReference())
                                    .put("lastName", o.getPatient().getLastName())
                                    .put("firstName", o.getPatient().getFirstName()))
                            .put("customerSignature", o.getCustomerSignature())
                            .put("medicationDispenses", JsonArray.map(o.getMedicationDispenses(), this::toJson))));
        });

        router.put(Pattern.compile("/\\d+/dispenseOrders/([-0-9a-f]+)"), request -> {
            Matcher match = Pattern.compile("/\\d+/dispenseOrders/([-0-9a-f]+)").matcher(req.getPathInfo());
            match.matches();

            DispenseOrder order = repository.getDispenseOrderById(match.group(1));

            return new JsonObject()
                .put("prices", pricesToJson(order))
                .put("patient", new JsonObject()
                        .put("id", order.getPatient().getReference())
                        .put("lastName", order.getPatient().getLastName())
                        .put("firstName", order.getPatient().getFirstName()))
                .put("packagingControlComplete", order.isPackagingControlComplete())
                .put("pharmacistControlComplete", order.isPharmacistControlComplete())
                .put("medicationDispenses", JsonArray.map(order.getMedicationDispenses(), this::toJson));
        });


        String pathInfo = req.getPathInfo();
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

    private JsonObject toJson(MedicationDispense dispense) {
        return new JsonObject()
            .put("id", dispense.getId())
            .put("printedDosageText", dispense.getPrintedDosageText())
            .put("scannedPackagingBarcode", "")
            .put("expectedDosageTextBarcode", dispense.getExpectedDosageBarcode())
            .put("scannedDosageLabel", "")
            .put("dispensedMedicationId", dispense.getMedicationId())
            .put("dispensedMedication", new JsonObject()
                    .put("gtin", dispense.getMedication() != null ? dispense.getMedication().getGtin() : null)
                    .put("display", dispense.getMedication() != null ? dispense.getMedication().getDisplay() : null))
            .put("price", toJson(dispense.getPrice()))
            .put("warningActions", JsonArray.map(dispense.getWarningActions(), action -> new JsonObject()
                    .put("id", action.getId())
                    .put("remark", action.getRemark())
                    .put("action", action.getAction())
                    .put("warningDisplay", action.getWarningDisplay())
                    .put("warningDetails", action.getWarningDetails())
                    .put("warningDetails2", action.getWarningDetails2())))
            .put("authorizingPrescription",
                    toJson(dispense.getAuthorizingPrescription(), dispense.isDispensed()));
    }

    private JsonObject pricesToJson(DispenseOrder order) {
        return new JsonObject()
                .put("patientCopay", toJson(order.getPatientTotal()))
                .put("patientUncovered", toJson(order.getUncoveredTotal()))
                .put("refundTotal", toJson(order.getRefundTotal()));
    }

    private JsonObject toJson(MedicationOrder prescription, boolean dispensed) {
        JsonObject json = new JsonObject()
                .put("medicationName", prescription.getMedicationName())
                .put("prescriber", new JsonObject()
                        .put("name", prescription.getPrescriber().getDisplay()))
                .put("dosageText", prescription.getDosageText())
                .put("dateWritten", prescription.getDateWritten().toString());
        if (!dispensed) {
            json.put("alternatives", JsonArray.map(prescription.getAlternatives(),
                    medication -> new JsonObject()
                    .put("trinnPrice", toJson(medication.getTrinnPrice()))
                    .put("productId", medication.getProductId())
                    .put("display", medication.getDisplay())
                    .put("details", medication.getDetails())));
        }
        return json;
    }

    private Object toJson(Money money) {
        return money != null ? money.format() : null;
    }

    private PharmacyPrincipal createPharmacyPrincipal(OpenIdPrincipal userPrincipal) {
        PharmacyPrincipal pharmacyPrincipal = new PharmacyPrincipal();
        pharmacyPrincipal.setName(userPrincipal.getName());
        pharmacyPrincipal.setDisplayName(userPrincipal.getDisplayName());
        pharmacyPrincipal.setJwtToken(userPrincipal.getToken().getIdToken());

        String[] herNumbers = userPrincipal.getToken()
                .claim("HER-numbers").orElse("").split(",?\\s+|,\\s*");
        for (String herNumber : herNumbers) {
            pharmacyPrincipal.getOrganizations().add(healthcareServiceRepository.retrieve(herNumber));
        }

        pharmacyPrincipal.setHprNumber(userPrincipal.getToken().claim("HPR-number").orElse(null));

        Optional<Practitioner> practitioner = practitionerRepository.getPractitioner(pharmacyPrincipal.getHprNumber());
        practitioner.ifPresent(p -> pharmacyPrincipal.setPractitioner(p));

        return pharmacyPrincipal;
    }

    // TODO: There are three ways we respond to a post - we need a unifying concept:
    //   1. Redirect (a persumed browser) to /pharmacies#<location>
    //   2. 201 Created with location header to a (presumed API) client
    //   3. Write JSON to the response (200 ok)s
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        PharmacyPrincipal principal = createPharmacyPrincipal((OpenIdPrincipal) req.getUserPrincipal());

        Map<Pattern, Function<HttpServletRequest, String>> router = new LinkedHashMap<>();
        router.put(Pattern.compile("/(\\d+)/prescriptionQueries"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/prescriptionQueries").matcher(req.getPathInfo());
            match.matches();

            JsonObject patient = JsonParser.parseToObject(req.getParameter("JSON"));
            Optional<String> nationalId = patient.stringValue("nationalId");
            String herNumber = match.group(1);
            UUID id = dispenseOrderService.startPrescriptionQuery(principal.getHprNumber(), herNumber, nationalId.get());

            return "/pharmacies/#" + herNumber + "/prescriptionQueries/" + id;
        });

        router.put(Pattern.compile("/(\\d+)/patientHistoryQuery"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/patientHistoryQuery").matcher(req.getPathInfo());
            match.matches();
            String herNumber = match.group(1);

            // TODO: This should be encapsulated and used everywhere
            JsonObject queryJson;
            if (request.getContentType().equals("application/json")) {
                queryJson = parseJsonObject(req);
            } else {
                queryJson = JsonParser.parseToObject(req.getParameter("JSON"));
            }


            // TODO Should save query time and use it to validate that it's only used for a limited time
            HealthRecordQuery query = new HealthRecordQuery();
            query.setOrganizationHerNumber(herNumber);
            query.setOperatorHprNumber(principal.getHprNumber());
            query.setOperatorJwtToken(principal.getJwtToken());
            query.setPatientId(UUID.fromString(queryJson.requiredString("patientId")));
            query.setPurposeAsString(queryJson.requiredString("purpose"));
            query.setDocumentation(queryJson.stringValue("documentation"));
            query.setRequestorIdType(queryJson.stringValue("requestorIdentitificationType"));
            query.setRequestorIdNumber(queryJson.stringValue("requestorIdentitificationNumber"));
            UUID id = healthRecordService.queryHealthRecord(query);

            if (request.getContentType().equals("application/json")) {
                resp.setStatus(201);
                resp.setHeader("Location",
                        AuthenticationConfiguration.getAuthority(req)
                        + req.getContextPath() + req.getServletPath() + "/"
                        + herNumber + "/patientHistoryQuery/" + id);
                return null;
            } else {
                return "/pharmacies/#" + herNumber + "/prescriptionQueries/" + id;
            }
        });

        router.put(Pattern.compile("/(\\d+)/queryPerson"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/queryPerson").matcher(req.getPathInfo());
            match.matches();

            JsonObject patient = parseJsonObject(req);
            Optional<String> nationalId = patient.stringValue("nationalId");
            String herNumber = match.group(1);

            PersonQuery personQuery = new PersonQuery();
            personQuery.setFirstName(patient.stringValue("firstName").orElse(null));
            personQuery.setLastName(patient.stringValue("lastName").orElse(null));
            personQuery.setNationalId(patient.stringValue("nationalId").orElse(null));
            List<PersonReference> result = patientRepository.queryPatient(personQuery);

            JsonArray response = JsonArray.map(result, person -> new JsonObject()
                    .put("firstName", person.getFirstName())
                    .put("lastName", person.getLastName())
                    .put("id", person.getReference()));

            resp.setContentType("application/json");
            try {
                response.toJson(resp.getWriter());
            } catch (IOException e) {
                throw ExceptionUtil.softenException(e);
            }
            return null;
        });

        router.put(Pattern.compile("/(\\d+)/dispenseOrders"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/dispenseOrders").matcher(req.getPathInfo());
            match.matches();

            JsonObject prescriptions = JsonParser.parseToObject(req.getParameter("JSON"));
            String herNumber = match.group(1);

            DispenseOrder order = dispenseOrderService.startDispenseOrder(prescriptions.requiredArray("prescriptions").strings());

            return "/pharmacies/#" + herNumber + "/dispenseOrders/" + order.getIdentifier();
        });

        router.put(Pattern.compile("/(\\d+)/dispenseOrders/([-0-9a-f]+)"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/dispenseOrders/([-0-9a-f]+)").matcher(req.getPathInfo());
            match.matches();

            String herNumber = match.group(1);

            DispenseOrder order = repository.getDispenseOrderById(match.group(2));

            JsonObject prescriptions = JsonParser.parseToObject(req.getParameter("JSON"));

            prescriptions.requiredArray("medicationDispenses").objectStream().forEach(
                    dispenseJson -> {
                        for (MedicationDispense dispense : order.getMedicationDispenses()) {
                            if (dispense.getId().equals(dispenseJson.requiredLong("id"))) {
                                Medication medication = dispenseJson.stringValue("dispensedMedicationId")
                                        .map(id ->
                                        id.isEmpty() ? null :  medicationRepository.findByProductId(id).get())
                                        .orElse(null);
                                dispense.setMedication(medication);
                                if (medication != null) {
                                    dispense.setPrice(Money.from(dispenseJson.requiredString("price")));
                                }
                                dispense.setPrintedDosageText(dispenseJson.requiredString("printedDosageText"));
                                dispense.setConfirmedByPharmacist(false);
                                dispense.setPackagingControlled(false);
                            }
                        }
                    });
            order.createWarnings();
            repository.update(order);

            return "/pharmacies/#" + herNumber + "/dispenseOrders/" + order.getIdentifier();
        });

        router.put(Pattern.compile("/(\\d+)/pharmacistOrders/([-0-9a-f]+)"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/pharmacistOrders/([-0-9a-f]+)").matcher(req.getPathInfo());
            match.matches();

            String herNumber = match.group(1);

            DispenseOrder order = repository.getDispenseOrderById(match.group(2));

            JsonObject prescriptions = JsonParser.parseToObject(req.getParameter("JSON"));

            prescriptions.requiredArray("medicationDispenses").objectStream().forEach(
                    dispenseJson -> {
                        for (MedicationDispense dispense : order.getMedicationDispenses()) {
                            if (dispense.getId().equals(dispenseJson.requiredLong("id"))) {
                                JsonArray array = dispenseJson.requiredArray("warningActions");
                                for (MedicationDispenseAction action : dispense.getWarningActions()) {
                                    JsonObject actionJson = array.objectStream()
                                        .filter(o -> o.requiredString("id").equals(action.getId()))
                                        .findFirst().get();
                                    action.setAction(actionJson.stringValue("action").orElse(null));
                                    action.setRemark(actionJson.stringValue("remark").orElse(null));
                                }
                            }
                            dispense.setConfirmedByPharmacist(dispense.isWarningsAddressed());
                        }
                    });
            repository.update(order);

            if (order.isPharmacistControlComplete()) {
                return "/pharmacies/#" + herNumber + "/pharmacistOrders/";
            } else {
                return "/pharmacies/#" + herNumber + "/pharmacistOrders/" + order.getIdentifier();
            }
        });

        router.put(Pattern.compile("/(\\d+)/confirmDispense/([-0-9a-f]+)"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/confirmDispense/([-0-9a-f]+)").matcher(req.getPathInfo());
            match.matches();

            String herNumber = match.group(1);

            DispenseOrder order = repository.getDispenseOrderById(match.group(2));

            JsonObject prescriptions = JsonParser.parseToObject(req.getParameter("JSON"));
            order.setCustomerSignature(prescriptions.requiredString("customerSignature"));
            order.setDispensed();
            repository.update(order);
            return "/pharmacies/#" + herNumber + "/index";
        });

        router.put(Pattern.compile("/(\\d+)/packageControl/([-0-9a-f]+)"), request -> {
            Matcher match = Pattern.compile("/(\\d+)/packageControl/([-0-9a-f]+)").matcher(req.getPathInfo());
            match.matches();

            String herNumber = match.group(1);

            DispenseOrder order = repository.getDispenseOrderById(match.group(2));

            JsonObject prescriptions = JsonParser.parseToObject(req.getParameter("JSON"));

            for (MedicationDispense dispense : order.getMedicationDispenses()) {
                Optional<JsonObject> dispenseJson = prescriptions.requiredArray("medicationDispenses").objectStream()
                        .filter(o -> dispense.getId().equals(o.requiredLong("id")))
                        .findFirst();
                dispenseJson.ifPresent(o -> {
                    String scannedDosageLabel = o.stringValue("scannedDosageLabel").orElse(null);
                    String scannedGtin = o.stringValue("scannedPackagingBarcode").orElse(null);

                    dispense.controlPackage(scannedDosageLabel, scannedGtin);
                });
                if (dispenseJson.isPresent()) {
                    String scannedDosageLabel = dispenseJson.get().stringValue("scannedDosageLabel").orElse(null);
                    String scannedGtin = dispenseJson.get().stringValue("scannedPackagingBarcode").orElse(null);
                    dispense.controlPackage(scannedDosageLabel, scannedGtin);
                } else {
                    dispense.controlPackage(null, null);
                }
            }
            repository.update(order);

            if (order.isPackagingControlComplete()) {
                return "/pharmacies/#" + herNumber + "/dispenseOrders/" + order.getIdentifier();
            } else {
                return "/pharmacies/#" + herNumber + "/packageControl/" + order.getIdentifier();
            }
        });

        for (Pattern pattern : router.keySet()) {
            if (pattern.matcher(pathInfo).matches()) {
                String location = router.get(pattern).apply(req);
                if (location != null) {
                    resp.sendRedirect(location);
                }
                return;
            }
        }

        super.doPost(req, resp);
    }

    private JsonObject parseJsonObject(HttpServletRequest req) {
        if (req.getContentType().equals("application/json")) {
            try {
                return JsonParser.parseToObject(req.getReader());
            } catch (IOException e) {
                throw ExceptionUtil.softenException(e);
            }
        } else {
            throw new IllegalArgumentException("Don't know how to deal with " + req.getContentType());
        }
    }

}
