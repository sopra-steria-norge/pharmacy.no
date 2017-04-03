package no.pharmacy.practitioner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.infrastructure.IOUtil;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class HprPractitionerImporter {

    private static final Logger logger = LoggerFactory.getLogger(HprPractitionerImporter.class);
    private Set<Long> existingHprNumbers;
    private JdbcPractitionerRepository repository;
    private CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withDelimiter(';').withFirstRecordAsHeader();

    HprPractitionerImporter(JdbcPractitionerRepository repository, JdbcSupport jdbcSupport) {
        this.repository = repository;
        existingHprNumbers = new HashSet<>(jdbcSupport.queryForList(
                "select hpr_number from practitioners",
                new ArrayList<>(), rs -> rs.getLong(1)));
    }

    public void refresh(String hprLocation) {
        if (hprLocation.endsWith(".zip")) {
            try (ZipInputStream zip = new ZipInputStream(new FileInputStream(hprLocation))) {
                ZipEntry zipEntry;
                while ((zipEntry = zip.getNextEntry()) != null) {
                    logger.info("zipEntry {}", zipEntry);
                    if (zipEntry.getName().endsWith("personer.csv")) {
                        savePeople(IOUtil.dontClose(zip));
                    } else if (zipEntry.getName().endsWith("godkjenninger.csv")) {
                        saveAuthorizations(IOUtil.dontClose(zip));
                    } else if (zipEntry.getName().endsWith("rekvisisjonsretter.csv")) {
                        savePrescriptionAuthorizations(IOUtil.dontClose(zip));
                    } else {
                        System.out.println("Huh!");
                    }
                }
            } catch (IOException e) {
                throw ExceptionUtil.softenException(e);
            }
        } else {
            try {
                Path root = Paths.get(hprLocation);
                if (Files.exists(root.resolve("personer.csv"))) {
                    savePeople(Files.newInputStream(root.resolve("personer.csv")));
                }
                if (Files.exists(root.resolve("godkjenninger.csv"))) {
                    saveAuthorizations(Files.newInputStream(root.resolve("godkjenninger.csv")));
                }
                if (Files.exists(root.resolve("rekvisisjonsretter.csv"))) {
                    savePrescriptionAuthorizations(Files.newInputStream(root.resolve("rekvisisjonsretter.csv")));
                }
            } catch (IOException e) {
                throw ExceptionUtil.softenException(e);
            }
        }

    }

    private void savePrescriptionAuthorizations(InputStream dontClose) {
        // TODO Auto-generated method stub

    }

    private void savePeople(InputStream input) {
        try (CSVParser parser = new CSVParser(new InputStreamReader(input, StandardCharsets.UTF_8), format)) {
            for (CSVRecord record : parser) {
                if (parser.getCurrentLineNumber() % 1000 == 0) {
                    logger.info("Reading persons, line {}", parser.getCurrentLineNumber());
                }

                Practitioner practitioner = new Practitioner();
                practitioner.setName(record.get("Fornavn") + " " + record.get("Etternavn"));
                practitioner.setIdentifier(Long.parseLong(record.get("HPRNummer")));
                if (!existingHprNumbers.contains(practitioner.getIdentifier())) {
                    existingHprNumbers.add(practitioner.getIdentifier());
                    repository.save(practitioner);
                }
            }

            logger.info("Completed saving people");
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }

    }

    private void saveAuthorizations(InputStream input) {
        try (CSVParser parser = new CSVParser(new InputStreamReader(input, StandardCharsets.UTF_8), format)) {
            for (CSVRecord record : parser) {
                if (parser.getCurrentLineNumber() % 1000 == 0) {
                    logger.info("Reading authorizations, line {}", parser.getCurrentLineNumber());
                }

                Long hprNumber = Long.parseLong(record.get("HPRNummer"));
                String authorization = record.get("Helsepersonellkategori");
                authorization = authorization.substring(authorization.lastIndexOf(":") + 1);

                if (existingHprNumbers.contains(hprNumber)) {
                    repository.saveAuthorization(hprNumber, authorization);
                }
            }
            logger.info("Completed saving authorizations");
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }
    }
}
