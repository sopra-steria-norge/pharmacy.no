package no.pharmacy.test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import no.pharmacy.core.Money;
import no.pharmacy.core.PersonReference;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.infrastructure.IOUtil;
import no.pharmacy.medication.FestMedicationImporter;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.medication.Medication;
import no.pharmacy.practitioner.Practitioner;

public class PharmaTestData {

    public static final String[] PRODUCT_IDS_WITH_SUBSTITUTIONS = {
            "038397", "048788", "048797", "089497", "438175", "010965", "022113", "183039", "458492", "088656", "043175", "023949", "164606", "587209", "397520", "414533", "004752", "011054", "578081", "004741", "038407", "081825", "159558", "071712", "164617", "114740", "022175", "079261", "423739", "038507", "010288", "088645", "435610", "500038", "164628", "403219", "595422", "384157", "509091", "184839"
    };
    public static final String[] PRODUCT_IDS_WITHOUT_SUBSTITUTIONS = {
            "482109", "298509", "206472", "362252", "340774", "539484", "160701", "025298",
    };

    private static int sequence;
    private static final List<Medication> medicationCache = new ArrayList<>();

    private static JdbcMedicationRepository medicationRepository;

    public synchronized JdbcMedicationRepository getMedicationRepository() {
        if (medicationRepository == null) {
            String jdbcUrl = System.getProperty("pharmacy.medication.jdbc.url", "jdbc:h2:file:./target/db/medications");
            DataSource dataSource = TestDataSource.createDataSource(jdbcUrl, "db/db-medications");
            medicationRepository = new JdbcMedicationRepository(dataSource);
            medicationRepository.refresh(IOUtil.url(System.getProperty("pharmacy.fest_source", FestMedicationImporter.FEST_URL.toString())));
        }
        return medicationRepository;
    }

    private static Random random = new Random();

    // TODO: Use a master list of national IDs known not to be used by anyone
    public List<String> unusedNationalIds(Random random, int count) {
        List<String> result = new ArrayList<>();
        for (int i=0; i<count; i++) {
            result.add(unusedNationalId(random));
        }
        Collections.sort(result);
        return result;
    }

    public String unusedNationalId(Random random) {
        // TODO: Also have some D-numbers
        LocalDate birthDate = LocalDate.now().minusYears(80)
                .plusDays(random.nextInt(80*365));
        return birthDate.format(DateTimeFormatter.ofPattern("ddMMyy"))
                + randomNumericString(5, random); // TODO: Calculate checksum
    }

    public String unusedNationalId() {
        return unusedNationalId(random);
    }

    private static String randomNumericString(int length) {
        return randomNumericString(length, random);
    }

    private static String randomNumericString(int length, Random random) {
        String result = "";
        for (int i=0; i<length; i++) {
            result += String.valueOf(random.nextInt(10));
        }
        return result;
    }

    public static int random(int i) {
        return random.nextInt(i);
    }

    @SafeVarargs
    public static <T> T pickOneOf(T... options) {
        return pickOneOf(random, options);
    }

    @SafeVarargs
    public static <T> T pickOneOf(Random random, T... options) {
        return options[random.nextInt(options.length)];
    }

    public static <T> T pickOne(List<T> options) {
        return options.get(random(options.size()));
    }

    public Practitioner samplePractitioner() {
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier(random(1000000));
        practitioner.setFirstName(sampleFirstName(random));
        practitioner.setLastName(sampleLastName(random));
        return practitioner;
    }

    public static String sampleName() {
        return sampleFirstName(random) + " " + sampleLastName(random);
    }

    public static String sampleLastName(Random random) {
        // https://www.ssb.no/a/navn/alf/etter100.html
        return pickOneOf(random, new String[] { "Hansen", "Johansen", "Olsen", "Larsen", "Andersen", "Pedersen", "Nilsen", "Kristiansen", "Jensen", "Karlsen", "Johnsen", "Pettersen", "Eriksen", "Berg", "Haugen" });
    }

    public static String sampleFirstName(Random random) {
        // https://www.ssb.no/befolkning/statistikker/navn/aar/2016-01-26?fokus=true
        String[] femaleNames = { "Emma", "Nora", "Sara", "Sofie", "Olivia", "Sofia", "Emilie", "Ella", "Leah", "Maja" };
        String[] maleNames = { "William", "Mathias", "Oliver", "Jakob", "Lucas", "Filip", "Liam", "Aksel", "Emil", "Oskar" };
        return random.nextBoolean() ?
                pickOneOf(random, femaleNames) : pickOneOf(random, maleNames);
    }

    public PersonReference sampleDoctor() {
        return new PersonReference(String.valueOf(randomId()),
                sampleFirstName(random), sampleLastName(random));
    }

    public static long randomId() {
        return sequence++;
    }

    public MedicationOrder sampleMedicationOrder(Medication medication) {
        return sampleMedicationOrder(sampleDoctor(), samplePastDate(), medication);
    }

    public MedicationOrder sampleMedicationOrder(PersonReference prescriber, LocalDate dateWritten, Medication medication) {
        MedicationOrder medicationOrder = new MedicationOrder();
        medicationOrder.setSubject(samplePatient());
        medicationOrder.setPrescriptionId(UUID.randomUUID().toString());
        medicationOrder.setPrescriber(prescriber);
        medicationOrder.setDateWritten(dateWritten);
        medicationOrder.setMedication(medication);
        medicationOrder.setDosageText(medication.getDisplay() + "\n\nTo tabelletter, morgen og kveld");
        return medicationOrder;
    }

    public MedicationOrder sampleMedicationOrder() {
        return sampleMedicationOrder(sampleDoctor(), samplePastDate(), sampleMedication());
    }

    public Medication getMedication(String productId) {
        return getMedicationRepository().findByProductId(productId).get();
    }

    public Medication sampleMedication() {
        if (medicationCache.isEmpty()) {
            medicationCache.addAll(getMedicationRepository().list());
        }
        return pickOne(medicationCache);
    }

    private LocalDate samplePastDate() {
        return LocalDate.now().minusDays(random(100));
    }




    public Medication medicationWithSubstitutes() {
        return getMedication(pickOneOf(PRODUCT_IDS_WITH_SUBSTITUTIONS));
    }

    public Medication medicationWithoutSubstitutes() {
        return getMedication(pickOneOf(PRODUCT_IDS_WITHOUT_SUBSTITUTIONS));
    }


    public static Money samplePrice() {
        return Money.inCents(random(2000) * 10);
    }

    public static <T extends Enum<T>> T pickOne(Class<T> enumClass) {
        return pickOneOf(enumClass.getEnumConstants());
    }

    public static String lorum() {
        List<String> words = new ArrayList<>();
        for (int i=0; i<random(20)+20; i++) {
            words.add(pickOneOf("Lorem", "ipsum", "dolor", "sit", "amet", "ut", "qui", "nisl", "munere", "nonumy", "Noster", "malorum", "at", "vim", "ad", "diceret", "scaevola", "pro", "Affert", "putant", "per", "no"));
        }
        return String.join(" ", words);
    }

    public String samplePng() {
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAACWCAYAAABkW7XSAAAEYklEQVR4Xu3UAQkAAAwCwdm/9HI83BLIOdw5AgQIRAQWySkmAQIEzmB5AgIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlAABg+UHCBDICBisTFWCEiBgsPwAAQIZAYOVqUpQAgQMlh8gQCAjYLAyVQlKgIDB8gMECGQEDFamKkEJEDBYfoAAgYyAwcpUJSgBAgbLDxAgkBEwWJmqBCVAwGD5AQIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlAABg+UHCBDICBisTFWCEiBgsPwAAQIZAYOVqUpQAgQMlh8gQCAjYLAyVQlKgIDB8gMECGQEDFamKkEJEDBYfoAAgYyAwcpUJSgBAgbLDxAgkBEwWJmqBCVAwGD5AQIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlAABg+UHCBDICBisTFWCEiBgsPwAAQIZAYOVqUpQAgQMlh8gQCAjYLAyVQlKgIDB8gMECGQEDFamKkEJEDBYfoAAgYyAwcpUJSgBAgbLDxAgkBEwWJmqBCVAwGD5AQIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlAABg+UHCBDICBisTFWCEiBgsPwAAQIZAYOVqUpQAgQMlh8gQCAjYLAyVQlKgIDB8gMECGQEDFamKkEJEDBYfoAAgYyAwcpUJSgBAgbLDxAgkBEwWJmqBCVAwGD5AQIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlAABg+UHCBDICBisTFWCEiBgsPwAAQIZAYOVqUpQAgQMlh8gQCAjYLAyVQlKgIDB8gMECGQEDFamKkEJEDBYfoAAgYyAwcpUJSgBAgbLDxAgkBEwWJmqBCVAwGD5AQIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlAABg+UHCBDICBisTFWCEiBgsPwAAQIZAYOVqUpQAgQMlh8gQCAjYLAyVQlKgIDB8gMECGQEDFamKkEJEDBYfoAAgYyAwcpUJSgBAgbLDxAgkBEwWJmqBCVAwGD5AQIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlAABg+UHCBDICBisTFWCEiBgsPwAAQIZAYOVqUpQAgQMlh8gQCAjYLAyVQlKgIDB8gMECGQEDFamKkEJEDBYfoAAgYyAwcpUJSgBAgbLDxAgkBEwWJmqBCVAwGD5AQIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlAABg+UHCBDICBisTFWCEiBgsPwAAQIZAYOVqUpQAgQMlh8gQCAjYLAyVQlKgIDB8gMECGQEDFamKkEJEDBYfoAAgYyAwcpUJSgBAgbLDxAgkBEwWJmqBCVAwGD5AQIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlAABg+UHCBDICBisTFWCEiBgsPwAAQIZAYOVqUpQAgQMlh8gQCAjYLAyVQlKgIDB8gMECGQEDFamKkEJEDBYfoAAgYyAwcpUJSgBAgbLDxAgkBEwWJmqBCVAwGD5AQIEMgIGK1OVoAQIGCw/QIBARsBgZaoSlACBB1YxAJfjJb2jAAAAAElFTkSuQmCC";
    }

    public static String sampleGtin() {
        return pickOneOf("70", "74", "40", "73") + randomNumericString(11);
    }

    public PersonReference samplePatient() {
        return new PersonReference(UUID.randomUUID().toString(), sampleFirstName(random), sampleLastName(random));
    }

    public static String sampleProductId() {
        return randomNumericString(6);
    }

    public String sampleHerNumber() {
        return randomNumericString(6);
    }

    public static LocalDate randomPastDate(int maxDaysAgo) {
        return LocalDate.now().minusDays(random(maxDaysAgo));
    }
}
