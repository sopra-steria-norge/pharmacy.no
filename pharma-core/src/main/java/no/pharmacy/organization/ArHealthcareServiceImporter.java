package no.pharmacy.organization;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Namespace;
import org.eaxy.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.infrastructure.jdbc.JdbcSupport;

public class ArHealthcareServiceImporter {
    private static final Logger logger = LoggerFactory.getLogger(ArHealthcareServiceImporter.class);

    private HealthcareServiceRepository healthcareServiceRepository;

    private Map<String, Instant> lastUpdated = new HashMap<>();

    public ArHealthcareServiceImporter(HealthcareServiceRepository healthcareServiceRepository,
            JdbcSupport jdbcSupport) {
        this.healthcareServiceRepository = healthcareServiceRepository;
        jdbcSupport.queryForList(
                "select her_number, updated_at from organizations",
                new ArrayList<>(), rs -> {
                    lastUpdated.put(rs.getString("her_number"),
                            rs.getTimestamp("updated_at").toInstant());
                    return rs.getLong(1);
                });
    }

    public void refresh(InputStream input) {
        try {
            logger.info("Refreshing HealthcareServices");
            Document doc = decodeXml(input);
            logger.info("Reading data");
            for (Element communicationParty : doc.find("CommunicationParty")) {
                ElementSet businessType = communicationParty.find("BusinessType", "CodeValue");
                if (businessType.isEmpty()) {
                    //logger.warn("No businessType for {}", name);
                } else if (businessType.first().text().equals("108") || businessType.first().text().equals("109")) {
                    HealthcareService organization = createOrganization(communicationParty);
                    if (!lastUpdated.containsKey(organization.getId())) {
                        healthcareServiceRepository.save(organization);
                    } else if (organization.getUpdatedAt().isAfter(lastUpdated.get(organization.getId()))) {
                        healthcareServiceRepository.update(organization);
                    }
                }
            }
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    private HealthcareService createOrganization(Element communicationParty) {
        HealthcareService organization = new HealthcareService();
        organization.setId(communicationParty.find("HerId").first().text());
        organization.setDisplay(communicationParty.find("Name").first().text());
        organization.setMunicipalityCode(communicationParty.find("Municipality", "CodeValue").first().text());
        organization.setBusinessType(communicationParty.find("BusinessType", "CodeValue").first().text());
        organization.setUpdatedAt(
                LocalDateTime.parse(communicationParty.find("LastChanged").first().text()).toInstant(ZoneOffset.UTC));
        return organization;
    }

    private Document decodeXml(InputStream input) throws IOException {
        return Xml.read(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    private static Namespace CP = new Namespace("http://register.nhn.no/CommunicationParty");

    public void createMini(String from, String to) {
        Element result = CP.el("ArrayOfCommunicationParty");

        try (FileInputStream input = new FileInputStream(from)) {
            logger.info("Minimizing HealthcareServices from {}", from);
            Document doc = decodeXml(input);
            logger.info("Reading data");
            for (Element communicationParty : doc.find("CommunicationParty")) {
                String name = communicationParty.find("Name").first().text();
                ElementSet businessType = communicationParty.find("BusinessType", "CodeValue");
                if (businessType.isEmpty()) {
                    //logger.warn("No businessType for {}", name);
                } else if (businessType.first().text().equals("108") || businessType.first().text().equals("109")) {
                    System.out.println(name);
                    result.add(communicationParty);
                }
            }
            try (Writer writer = new FileWriter(to)) {
                Xml.doc(result).writeTo(writer);
            }
        } catch (IOException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

}
