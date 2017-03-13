package no.pharmacy.medication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;

import no.pharmacy.infrastructure.IOUtil;

public class DownloadMedicationsFromFest {

    private JdbcMedicationRepository medicationRepository;

    public DownloadMedicationsFromFest(JdbcMedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public void downloadFestFile(Connection conn) throws Exception {
        File festFile = new File("target/fest251.zip");
        IOUtil.copy(new URL("https://www.legemiddelsok.no/_layouts/15/FESTmelding/fest251.zip"), festFile);

        Document festDoc = null;
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(festFile))) {
            ZipEntry zipEntry;
            while((zipEntry = zip.getNextEntry()) != null) {
                if (zipEntry.getName().equals("fest251.xml")) {
                    int byteOrderMark = zip.read(); // \uEFBBBF
                    if (byteOrderMark == 0xef) {
                        zip.read(); zip.read();
                    }

                    festDoc = Xml.read(new InputStreamReader(zip));
                    break;
                }
            }
        }
        if (festDoc == null) {
            throw new IllegalArgumentException();
        }

        saveFest(festDoc, conn);
    }

    private void saveFest(Document festDoc, Connection conn) throws SQLException {
        FestMedicationImporter importer = new FestMedicationImporter();
        for (Medication medication : importer.readMedicationPackage(festDoc.find("KatLegemiddelpakning").first())) {
            medicationRepository.save(medication, conn);
        }
        for (Element byttegruppe : festDoc.find("KatByttegruppe").first().elements()) {
            System.out.println(byttegruppe.toXML());
        }
        System.out.println(festDoc);
    }


    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:file:" + new File("target/db/medications").getAbsolutePath();
        JdbcConnectionPool dataSource = JdbcConnectionPool.create(url, "sa", "");

        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("db/db-medications");
        flyway.migrate();

        try (Connection conn = dataSource.getConnection()) {
            new DownloadMedicationsFromFest(new JdbcMedicationRepository(dataSource)).downloadFestFile(conn);
        }
    }
}
