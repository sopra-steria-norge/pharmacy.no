package no.pharmacy.test;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;

import lombok.Getter;
import no.pharmacy.core.Money;
import no.pharmacy.core.Practitioner;
import no.pharmacy.medication.JdbcMedicationRepository;
import no.pharmacy.medication.Medication;
import no.pharmacy.order.MedicationOrder;

public class PharmaTestData {

    private static int sequence;
    private final List<Medication> medicationCache = new ArrayList<>();

    @Getter
    private final JdbcMedicationRepository medicationRepository;

    private static Random random = new Random();

    public PharmaTestData() {
        File currentDir = new File(System.getProperty("user.dir"));
        File dbFile = new File(currentDir, "target/db/medications");
        String url = "jdbc:h2:file:" + dbFile.getAbsolutePath();

        DataSource medicationTestDatasource = JdbcConnectionPool.create(url, "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(medicationTestDatasource);
        flyway.setLocations("db/db-medications");
        flyway.migrate();

        this.medicationRepository = new JdbcMedicationRepository(medicationTestDatasource);
        medicationRepository.refresh();
    }


    // TODO: Use a master list of national IDs known not to be used by anyone
    public static List<String> unusedNationalIds(int count) {
        List<String> result = new ArrayList<>();
        for (int i=0; i<count; i++) {
            result.add(unusedNationalId());
        }
        Collections.sort(result);
        return result;
    }

    private static String unusedNationalId() {
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

    public List<Medication> sampleMedications(int count, FakeMedicationSource fakeMedicationSource) {
        List<Medication> medications = new ArrayList<>();
        for (int i=0; i<count; i++) {
            medications.add(sampleMedication(fakeMedicationSource));
        }
        medications.sort((a, b) -> a.getDisplay().compareTo(b.getDisplay()));
        return medications;
    }

    public Medication sampleMedication(FakeMedicationSource fakeMedicationSource) {
        return fakeMedicationSource.pickOne();
    }

    public static <T> T pickOneOf(T... options) {
        return options[random(options.length)];
    }

    public static <T> T pickOne(List<T> options) {
        return options.get(random(options.size()));
    }

    public Practitioner samplePractitioner() {
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier(random(1000000));
        practitioner.setName(sampleName());
        return practitioner;
    }

    private String sampleName() {
        return sampleFirstName() + " " + sampleLastName();
    }

    private String sampleLastName() {
        // https://www.ssb.no/a/navn/alf/etter100.html
        return pickOneOf(new String[] { "Hansen", "Johansen", "Olsen", "Larsen", "Andersen", "Pedersen", "Nilsen", "Kristiansen", "Jensen", "Karlsen", "Johnsen", "Pettersen", "Eriksen", "Berg", "Haugen" });
    }

    private String sampleFirstName() {
        // https://www.ssb.no/befolkning/statistikker/navn/aar/2016-01-26?fokus=true
        String[] femaleNames = { "Emma", "Nora", "Sara", "Sofie", "Olivia", "Sofia", "Emilie", "Ella", "Leah", "Maja" };
        String[] maleNames = { "William", "Mathias", "Oliver", "Jakob", "Lucas", "Filip", "Liam", "Aksel", "Emil", "Oskar" };
        return chance(50) ? pickOneOf(femaleNames) : pickOneOf(maleNames);
    }

    private boolean chance(int percent) {
        return random(100) < percent;
    }

    public static Practitioner sampleDoctor() {
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier(randomId());
        practitioner.setName("Random J Doctor");
        return practitioner;
    }

    public static long randomId() {
        return sequence++;
    }

    public MedicationOrder sampleMedicationOrder(Medication medication) {
        return sampleMedicationOrder(sampleDoctor(), samplePastDate(), medication);
    }

    public static MedicationOrder sampleMedicationOrder(Practitioner prescriber, LocalDate dateWritten, Medication medication) {
        MedicationOrder medicationOrder = new MedicationOrder();
        medicationOrder.setPrescriber(prescriber.getReference());
        medicationOrder.setDateWritten(dateWritten);
        medicationOrder.setMedication(medication);
        return medicationOrder;
    }

    public MedicationOrder sampleMedicationOrder() {
        return sampleMedicationOrder(sampleDoctor(), samplePastDate(), sampleMedication());
    }

    public Medication sampleMedication(String productId) {
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

    private Medication sampleMedication() {
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


    public Money samplePrice() {
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
}
