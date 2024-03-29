package no.pharmacy.practitioner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class HprPractitionerImporter {

    private static final Logger logger = LoggerFactory.getLogger(HprPractitionerImporter.class);
    private Map<Long, Instant> lastUpdated = new HashMap<>();
    private JdbcPractitionerRepository repository;
    private CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withDelimiter(';').withFirstRecordAsHeader();
    private DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    private JdbcSupport jdbcSupport;

    HprPractitionerImporter(JdbcPractitionerRepository repository, JdbcSupport jdbcSupport) {
        this.repository = repository;
        this.jdbcSupport = jdbcSupport;
    }

    private void cachePractitioners(JdbcSupport jdbcSupport) {
        jdbcSupport.queryForList(
                "select hpr_number, updated_at from practitioners",
                new ArrayList<>(), rs -> {
                    lastUpdated.put(rs.getLong("hpr_number"),
                            rs.getTimestamp("updated_at").toInstant());
                    return rs.getLong(1);
                });
    }

    public void refresh(URL url) throws IOException {
        logger.info("Reading Practitioners from {}", url);
        if (url.openConnection().getLastModified() < repository.lastImportTime(url)) {
            logger.info("Skipping up to date {}", url);
            return;
        }
        cachePractitioners(jdbcSupport);

        if (url.toString().endsWith(".zip")) {
            try(ZipFile file = new ZipFile(new File(url.getFile()))) {
                savePeople(file.getInputStream(file.getEntry("normalisert\\personer.csv")));
                saveAuthorizations(file.getInputStream(file.getEntry("normalisert\\godkjenninger.csv")));
            }
        } else {
            savePeople(new URL(url, "personer.csv").openStream());
            saveAuthorizations(new URL(url, "godkjenninger.csv").openStream());
        }
        repository.updateLastImportTime(System.currentTimeMillis(), url);
    }

    private void savePeople(InputStream input) {
        try (CSVParser parser = new CSVParser(new InputStreamReader(input, StandardCharsets.UTF_8), format)) {
            for (CSVRecord record : parser) {
                if (parser.getCurrentLineNumber() % 10000 == 0) {
                    logger.info("Reading persons, line {}", parser.getCurrentLineNumber());
                }

                Practitioner practitioner = toPractitioner(record);
                if (practitioner == null) continue;
                Instant previousUpdate = lastUpdated.get(practitioner.getIdentifier());
                if (previousUpdate == null) {
                    repository.save(practitioner);
                    lastUpdated.put(practitioner.getIdentifier(), practitioner.getUpdatedAt());
                } else if (practitioner.getUpdatedAt().isAfter(previousUpdate)) {
                    repository.update(practitioner);
                    lastUpdated.put(practitioner.getIdentifier(), practitioner.getUpdatedAt());
                }
            }
            logger.info("Completed saving {} people", parser.getRecordNumber());
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }

    }

    private Practitioner toPractitioner(CSVRecord record) {
        try {
            Practitioner practitioner = new Practitioner();
            practitioner.setFirstName(record.get("Fornavn"));
            practitioner.setLastName(record.get("Etternavn"));
            practitioner.setNationalId(record.get("Personnummer"));
            practitioner.setIdentifier(Long.parseLong(record.get("HPRNummer")));
            practitioner.setUpdatedAt(
                    datePattern.parse(record.get("SistOppdatert"), LocalDateTime::from).atZone(ZoneId.systemDefault()).toInstant());

            String dateOfBirth = record.get("Fødselsdato");
            if (dateOfBirth.isEmpty()) {
                logger.warn("Problem on {}: no date of birth for {}", record.getRecordNumber(), practitioner);
                return null;
            }
            practitioner.setDateOfBirth(
                    datePattern.parse(dateOfBirth, LocalDate::from));
            return practitioner;
        } catch (RuntimeException e) {
            logger.warn("Error on " + record.getRecordNumber());
            throw e;
        }
    }

    private void saveAuthorizations(InputStream input) {
        Set<Long> authorizationIds = new HashSet<>(jdbcSupport.queryForList(
                "select id from practitioner_authorizations",
                new ArrayList<>(), rs -> rs.getLong(1)));
        try (CSVParser parser = new CSVParser(new InputStreamReader(input, StandardCharsets.UTF_8), format)) {
            for (CSVRecord record : parser) {
                if (parser.getCurrentLineNumber() % 10000 == 0) {
                    logger.info("Reading authorizations, line {}", parser.getCurrentLineNumber());
                }

                Long hprNumber = Long.parseLong(record.get("HPRNummer"));
                String authorization = record.get("Helsepersonellkategori");
                authorization = authorization.substring(authorization.lastIndexOf(":") + 1);
                long id = Long.parseLong(record.get("Id"));

                if (!authorizationIds.contains(id) && lastUpdated.containsKey(hprNumber)) {
                    repository.saveAuthorization(id, hprNumber, authorization);
                }
            }
            logger.info("Completed saving {} authorizations", parser.getCurrentLineNumber());
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }
    }
}
