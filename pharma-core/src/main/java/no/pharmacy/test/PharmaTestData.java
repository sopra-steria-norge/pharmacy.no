package no.pharmacy.test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import no.pharmacy.core.Practitioner;
import no.pharmacy.medication.Medication;

public class PharmaTestData {

    // TODO: Use a master list of national IDs known not to be used by anyone
    public List<String> unusedNationalIds(int count) {
        List<String> result = new ArrayList<>();
        for (int i=0; i<count; i++) {
            result.add(unusedNationalId());
        }
        Collections.sort(result);
        return result;
    }

    private String unusedNationalId() {
        // TODO: Also have some D-numbers
        LocalDate birthDate = LocalDate.now().minusYears(80)
                .plusDays(random(80*365));
        return birthDate.format(DateTimeFormatter.ofPattern("ddMMyy"))
                + randomNumericString(5); // TODO: Calculate checksum
    }

    private String randomNumericString(int length) {
        String result = "";
        for (int i=0; i<length; i++) {
            result += String.valueOf(random(10));
        }
        return result;
    }

    private int random(int i) {
        Random random = new Random();
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

    private <T> T pickOne(T[] options) {
        return options[random(options.length)];
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
        return pickOne(new String[] { "Hansen", "Johansen", "Olsen", "Larsen", "Andersen", "Pedersen", "Nilsen", "Kristiansen", "Jensen", "Karlsen", "Johnsen", "Pettersen", "Eriksen", "Berg", "Haugen" });
    }

    private String sampleFirstName() {
        // https://www.ssb.no/befolkning/statistikker/navn/aar/2016-01-26?fokus=true
        String[] femaleNames = { "Emma", "Nora", "Sara", "Sofie", "Olivia", "Sofia", "Emilie", "Ella", "Leah", "Maja" };
        String[] maleNames = { "William", "Mathias", "Oliver", "Jakob", "Lucas", "Filip", "Liam", "Aksel", "Emil", "Oskar" };
        return chance(50) ? pickOne(femaleNames) : pickOne(maleNames);
    }

    private boolean chance(int percent) {
        return random(100) < percent;
    }

}
