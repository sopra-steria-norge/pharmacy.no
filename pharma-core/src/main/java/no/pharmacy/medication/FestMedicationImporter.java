package no.pharmacy.medication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.input.BOMInputStream;
import org.eaxy.Element;
import org.eaxy.ElementFilters;
import org.eaxy.ElementSet;
import org.eaxy.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.core.Money;
import no.pharmacy.core.Reference;
import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.infrastructure.IOUtil;

public class FestMedicationImporter {
    private Validator validator = new Validator(new String[] { "R1808-eResept-M30-2014-12-01/ER-M30-2014-12-01.xsd" });

    public static final URL FEST_URL = IOUtil.url("https://www.legemiddelsok.no/_layouts/15/FESTmelding/fest251.zip");

    private static final Logger logger = LoggerFactory.getLogger(FestMedicationImporter.class);

    void saveFest(URL url, JdbcMedicationRepository repository) {
        if (url.getPath().endsWith("zip")) {
            downloadFestDoc(url, repository);
        } else if (url.getPath().endsWith(".gz")) {
            try (Reader reader = new InputStreamReader(new GZIPInputStream(url.openStream()))) {
                saveFest(reader, repository);
            } catch (IOException e) {
                throw ExceptionUtil.softenException(e);
            }
        } else {
            try (Reader reader = new InputStreamReader(url.openStream())) {
                saveFest(reader, repository);
            } catch (IOException e) {
                throw ExceptionUtil.softenException(e);
            }
        }
    }


    void downloadFestDoc(URL url, JdbcMedicationRepository repository) {
        if (System.getProperty("pharmacy.disable_fest_refresh") != null) {
            throw new IllegalStateException("FEST refresh is disabled");
        }
        try {
            File festFile = new File("target/fest251.zip");

            logger.info("Downloading {}", url);
            IOUtil.copy(url, festFile, new File("target/tmp"));
            logger.info("Downloaded {} into {}", url, festFile);

            try (ZipInputStream zip = new ZipInputStream(new FileInputStream(festFile))) {
                try(InputStream entry = new BOMInputStream(IOUtil.zipEntry(zip, "fest251.xml"))) {
                    logger.info("Reading fest251.xml");
                    saveFest(new InputStreamReader(entry), repository);
                    logger.info("Import complete");
                }
            }
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }
    }


    List<Reference> readMedicationGroup(Element oppfByttegruppe) {
        validator.validate(oppfByttegruppe);

        List<Reference> result = new ArrayList<>();
        for (Element byttegruppe : oppfByttegruppe.elements()) {
            Element byttegruppeKode = byttegruppe.find("Byttegruppe", "Kode").first();
            result.add(new Reference(byttegruppeKode.attr("V"), byttegruppeKode.attr("DN")));
        }

        return result;
    }


    Medication readMedication(Element oppfLegemiddelpakning) {
        Element legemiddelpakning = oppfLegemiddelpakning.find("Legemiddelpakning").first();
        Medication medication = new Medication();
        medication.setDisplay(legemiddelpakning.find("NavnFormStyrke").firstTextOrNull());
        medication.setProductId(legemiddelpakning.find("Varenr").firstTextOrNull());
        medication.setGtin(legemiddelpakning.find("Ean").firstTextOrNull());
        medication.setSubstitutionGroup(legemiddelpakning.find("PakningByttegruppe", "RefByttegruppe").firstTextOrNull());
        medication.setTrinnPrice(getTrinnPrice(legemiddelpakning));
        ElementSet atcCodes = legemiddelpakning.find("Atc");
        if (atcCodes.isPresent()) {
            medication.setSubstance(atcCodes.first().attr("V"));
        }
        medication.setXml(legemiddelpakning.toXML());
        return medication;
    }


    private Money getTrinnPrice(Element legemiddelpakning) {
        for (Element element : legemiddelpakning.find("PrisVare")) {
            if (!element.find("Type[V=5]").isEmpty()) {
                return Money.from(element.find("Pris").first().attr("V"));
            }
        }
        return null;
    }

    MedicationInteraction readInteraction(Element oppfInteraksjon) {
        ElementSet interactions = oppfInteraksjon.find("Interaksjon");
        if (interactions.isEmpty()) {
            return null;
        }
        Element interaksjon = interactions.first();
        MedicationInteraction interaction = new MedicationInteraction();
        interaction.setId(interaksjon.find("Id").first().text());
        interaction.setClinicalConsequence(interaksjon.find("KliniskKonsekvens").firstTextOrNull());
        interaction.setInteractionMechanism(interaksjon.find("Interaksjonsmekanisme").firstTextOrNull());
        interaction.setSeverity(MedicalInteractionSeverity.byValue(interaksjon.find("Relevans").first().attr("V")));

        for (Element substance : interaksjon.find("Substansgruppe")) {
            // TODO: This is incorrect if one Substansgruppe has several Substans
            ElementSet atcCodes = substance.find("Substans", "Atc");
            if (atcCodes.isEmpty()) {
                return null;
            }
            interaction.getSubstanceCodes().add(atcCodes.first().attr("V"));
        }
        return interaction;
    }

    private void saveFest(Reader festDoc, MedicationRepository medicationRepository) {
        for (Element element : ElementFilters.create("*", "*").iterate(festDoc)) {
            if (element.tagName().equals("OppfLegemiddelpakning")) {
                medicationRepository.save(readMedication(element));
            } else if (element.tagName().equals("OppfInteraksjon")) {
                medicationRepository.save(readInteraction(element));
            } else if (!element.tagName().startsWith("Oppf")) {
                throw new IllegalArgumentException("Unexpected element " + element);
            }
        }
    }

}
