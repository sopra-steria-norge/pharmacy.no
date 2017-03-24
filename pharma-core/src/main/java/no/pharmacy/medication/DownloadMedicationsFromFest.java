package no.pharmacy.medication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eaxy.Document;
import org.eaxy.Xml;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.infrastructure.IOUtil;

public class DownloadMedicationsFromFest {

    private static final Logger logger = LoggerFactory.getLogger(DownloadMedicationsFromFest.class);

    private JdbcMedicationRepository medicationRepository;

    public DownloadMedicationsFromFest(JdbcMedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public void downloadFestFile() throws Exception {
        File festFile = new File("target/fest251.zip");

        URL festUrl = new URL("https://www.legemiddelsok.no/_layouts/15/FESTmelding/fest251.zip");
        logger.info("Downloading {}", festUrl);
        IOUtil.copy(festUrl, festFile);
        logger.info("Downloaded {} into {}", festUrl, festFile);

        Document festDoc = null;
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(festFile))) {
            ZipEntry zipEntry;
            while((zipEntry = zip.getNextEntry()) != null) {
                if (zipEntry.getName().equals("fest251.xml")) {
                    int byteOrderMark = zip.read(); // \uEFBBBF
                    if (byteOrderMark == 0xef) {
                        zip.read(); zip.read();
                    }

                    logger.info("Reading {}", zipEntry);
                    festDoc = Xml.read(new InputStreamReader(zip));
                    logger.info("Read complete", zipEntry);
                    break;
                }
            }
        }
        if (festDoc == null) {
            throw new IllegalArgumentException();
        }

        saveFest(festDoc);
    }

    private void saveFest(Document festDoc) {
        FestMedicationImporter importer = new FestMedicationImporter();
        logger.info("Inserting medications");
        for (Medication medication : importer.readMedicationPackages(festDoc.find("KatLegemiddelpakning").first())) {
            medicationRepository.save(medication);
        }
        logger.info("Inserted medications");

        logger.info("Inserting interactions");
        for (MedicationInteraction interaction : importer.readInteractions(festDoc.find("KatInteraksjon").first())) {
            medicationRepository.save(interaction);
        }
        logger.info("Inserted interactions");
    }


    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:file:" + new File("target/db/medications").getAbsolutePath();
        JdbcConnectionPool medicationDataSource = JdbcConnectionPool.create(url, "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(medicationDataSource);
        flyway.setLocations("db/db-medications");
        flyway.migrate();

        try (Connection conn = medicationDataSource.getConnection()) {
            new DownloadMedicationsFromFest(new JdbcMedicationRepository(medicationDataSource)).downloadFestFile();
        }
    }
}
