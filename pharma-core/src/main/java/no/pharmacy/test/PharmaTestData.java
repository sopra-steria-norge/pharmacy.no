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
import no.pharmacy.medication.FestMedicationImporter;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.medication.Medication;
import no.pharmacy.practitioner.Practitioner;

public class PharmaTestData {

    private static int sequence;
    private static final List<Medication> medicationCache = new ArrayList<>();

    private final static JdbcMedicationRepository medicationRepository = new JdbcMedicationRepository(medicationDataSource());

    public JdbcMedicationRepository getMedicationRepository() {
        return medicationRepository;
    }

    static {
        medicationRepository.refresh(System.getProperty("pharmacy.fest_source", FestMedicationImporter.FEST_URL.toString()));
    }

    private static Random random = new Random();
    private static DataSource medicationTestDatasource;

    public static synchronized DataSource medicationDataSource() {
        if (medicationTestDatasource == null) {
            medicationTestDatasource = TestDataSource.createDataSource("pharmacy.medication.jdbc.url", "jdbc:h2:file:./target/db/medications", "db/db-medications");
        }
        return medicationTestDatasource;
    }


    // TODO: Use a master list of national IDs known not to be used by anyone
    public List<String> unusedNationalIds(int count) {
        List<String> result = new ArrayList<>();
        for (int i=0; i<count; i++) {
            result.add(unusedNationalId());
        }
        Collections.sort(result);
        return result;
    }

    public String unusedNationalId() {
        // TODO: Also have some D-numbers
        LocalDate birthDate = LocalDate.now().minusYears(80)
                .plusDays(random(80*365));
        return birthDate.format(DateTimeFormatter.ofPattern("ddMMyy"))
                + randomNumericString(5); // TODO: Calculate checksum
    }

    private static String randomNumericString(int length) {
        String result = "";
        for (int i=0; i<length; i++) {
            result += String.valueOf(random(10));
        }
        return result;
    }

    public static int random(int i) {
        return random.nextInt(i);
    }

    @SafeVarargs
    public static <T> T pickOneOf(T... options) {
        return options[random(options.length)];
    }

    public static <T> T pickOne(List<T> options) {
        return options.get(random(options.size()));
    }

    public Practitioner samplePractitioner() {
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier(random(1000000));
        practitioner.setFirstName(sampleFirstName());
        practitioner.setLastName(sampleLastName());
        return practitioner;
    }

    public static String sampleName() {
        return sampleFirstName() + " " + sampleLastName();
    }

    private static String sampleLastName() {
        // https://www.ssb.no/a/navn/alf/etter100.html
        return pickOneOf(new String[] { "Hansen", "Johansen", "Olsen", "Larsen", "Andersen", "Pedersen", "Nilsen", "Kristiansen", "Jensen", "Karlsen", "Johnsen", "Pettersen", "Eriksen", "Berg", "Haugen" });
    }

    private static String sampleFirstName() {
        // https://www.ssb.no/befolkning/statistikker/navn/aar/2016-01-26?fokus=true
        String[] femaleNames = { "Emma", "Nora", "Sara", "Sofie", "Olivia", "Sofia", "Emilie", "Ella", "Leah", "Maja" };
        String[] maleNames = { "William", "Mathias", "Oliver", "Jakob", "Lucas", "Filip", "Liam", "Aksel", "Emil", "Oskar" };
        return chance(50) ? pickOneOf(femaleNames) : pickOneOf(maleNames);
    }

    private static boolean chance(int percent) {
        return random(100) < percent;
    }

    public PersonReference sampleDoctor() {
        return new PersonReference(String.valueOf(randomId()),
                sampleFirstName(), sampleLastName());
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
        if (medicationCache.isEmpty()) {
            medicationCache.addAll(medicationRepository.list());
        }

        for (Medication medication : medicationCache) {
            if (medication.getProductId().equals(productId)) {
                return medication;
            }
        }
        throw new IllegalArgumentException("Can't find medication " + productId);
    }

    public Medication sampleMedication() {
        if (medicationCache.isEmpty()) {
            medicationCache.addAll(medicationRepository.list());
        }

        return pickOne(medicationCache);
    }

    private LocalDate samplePastDate() {
        return LocalDate.now().minusDays(random(100));
    }




    public Medication medicationWithSubstitutes() {
        String[] id = {
                "038397", "048788", "048797", "089497", "438175", "010965", "022113", "183039", "458492", "088656", "043175", "023949", "164606", "587209", "397520", "414533", "004752", "011054", "578081", "004741", "038407", "081825", "159558", "071712", "164617", "114740", "022175", "079261", "423739", "038507", "010288", "088645", "435610", "500038", "164628", "403219", "595422", "384157", "509091", "184839"
        };
        return medicationRepository.findByProductId(pickOneOf(id)).get();
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
        return pickOneOf("70", "74", "40", "73")
                + randomNumericString(11);
    }

    public PersonReference samplePatient() {
        return new PersonReference(UUID.randomUUID().toString(), sampleFirstName(), sampleLastName());
    }

    public static String sampleProductId() {
        return randomNumericString(6);
    }
}
