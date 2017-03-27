package no.pharmacy.medication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Validator;
import org.eaxy.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.core.Money;
import no.pharmacy.core.Reference;
import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.infrastructure.IOUtil;
import no.pharmacy.test.PharmaTestData;

public class FestMedicationImporter {
    private Validator validator = new Validator(new String[] { "R1808-eResept-M30-2014-12-01/ER-M30-2014-12-01.xsd" });

    public static final URL FEST_URL = IOUtil.url("https://www.legemiddelsok.no/_layouts/15/FESTmelding/fest251.zip");

    private static final Logger logger = LoggerFactory.getLogger(FestMedicationImporter.class);

    public void saveFest(URL url, JdbcMedicationRepository repository) {
        try {
            if (url.getProtocol().startsWith("http")) {
                saveFest(downloadFestDoc(url), repository);
            } else {
                saveFest(Xml.read(new File(url.getFile())), repository);
            }
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }
    }


    public static Document downloadFestDoc(URL url) {
        if (System.getProperty("pharmacy.disable_fest_refresh") != null) {
            throw new IllegalStateException("FEST refresh is disabled");
        }
        try {
            File festFile = new File("target/fest251.zip");

            logger.info("Downloading {}", url);
            IOUtil.copy(url, festFile);
            logger.info("Downloaded {} into {}", url, festFile);

            try (ZipInputStream zip = new ZipInputStream(new FileInputStream(festFile))) {
                try(InputStream entry = IOUtil.zipEntry(zip, "fest251.xml")) {
                    int byteOrderMark = entry.read(); // \uEFBBBF
                    if (byteOrderMark == 0xef) {
                        entry.read(); entry.read();
                    }
                    logger.info("Reading fest251.xml");
                    Document festDoc = Xml.read(new InputStreamReader(zip));
                    logger.info("Read complete");
                    return festDoc;
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


    List<Medication> readMedicationPackages(Element katLegemiddelpakning) { List<Medication> result = new ArrayList<>();
        for (Element oppfLegemiddelpakning : katLegemiddelpakning.elements()) {
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
            result.add(medication);
        }

        return result;
    }


    private Money getTrinnPrice(Element legemiddelpakning) {
        for (Element element : legemiddelpakning.find("PrisVare")) {
            if (!element.find("Type[V=5]").isEmpty()) {
                return Money.from(element.find("Pris").first().attr("V"));
            }
        }
        return null;
    }


    List<MedicationInteraction> readInteractions(Element katInteraksjon) {
        ArrayList<MedicationInteraction> result = new ArrayList<>();
        interaction: for (Element interaksjon : katInteraksjon.find("OppfInteraksjon", "Interaksjon")) {
            MedicationInteraction interaction = new MedicationInteraction();
            interaction.setId(interaksjon.find("Id").first().text());
            interaction.setClinicalConsequence(interaksjon.find("KliniskKonsekvens").firstTextOrNull());
            interaction.setInteractionMechanism(interaksjon.find("Interaksjonsmekanisme").firstTextOrNull());
            interaction.setSeverity(MedicalInteractionSeverity.byValue(interaksjon.find("Relevans").first().attr("V")));

            for (Element substance : interaksjon.find("Substansgruppe")) {
                // TODO: This is incorrect if one Substansgruppe has several Substans
                ElementSet atcCodes = substance.find("Substans", "Atc");
                if (atcCodes.isEmpty()) {
                    continue interaction;
                }
                interaction.getSubstanceCodes().add(atcCodes.first().attr("V"));
            }

            result.add(interaction);
        }
        return result;
    }


    private void saveFest(Document festDoc, MedicationRepository medicationRepository) {
        logger.info("Inserting medications");
        for (Medication medication : readMedicationPackages(festDoc.find("KatLegemiddelpakning").first())) {
            medicationRepository.save(medication);
        }
        logger.info("Inserted medications");

        logger.info("Inserting interactions");
        for (MedicationInteraction interaction : readInteractions(festDoc.find("KatInteraksjon").first())) {
            medicationRepository.save(interaction);
        }
        logger.info("Inserted interactions");
    }

    public static void main(String[] args) {
        JdbcMedicationRepository repository = new JdbcMedicationRepository(PharmaTestData.medicationDataSource());
        FestMedicationImporter importer = new FestMedicationImporter();
        importer.saveFest(FEST_URL, repository);
    }
}
