package no.pharmacy.medication;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Random;

import javax.sql.DataSource;

import org.eaxy.Element;
import org.eaxy.Namespace;
import org.eaxy.Validator;
import org.eaxy.Xml;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Test;

import no.pharmacy.order.Reference;

public class FestMedicationImporterTest {

    private static final Namespace M30 = new Namespace("http://www.kith.no/xmlstds/eresept/m30/2014-12-01", "m30");
    private static final Namespace F = new Namespace("http://www.kith.no/xmlstds/eresept/forskrivning/2014-12-01", "f");
    private FestMedicationImporter importer = new FestMedicationImporter();
    private Validator validator = Xml.validatorFromResource("R1808-eResept-M30-2014-12-01/ER-M30-2014-12-01.xsd");
    private Random random = new Random();


    @Test
    public void readExchangeGroup() throws Exception {
        Reference expectedExchangeGroup = new Reference("000905", "EPROSARTAN TABLETTER 600MG");

        Element oppfByttegruppe = M30.el("OppfByttegruppe",
                M30.el("Id", "ID_51BCDA82-1EAA-454A-932B-7E716F5AC484"),
                M30.el("Tidspunkt", "2014-12-06T01:16:07"),
                M30.el("Status").attr("V", "A").attr("DN", "Aktiv oppføring"),
                M30.el("Byttegruppe",
                        M30.el("Id", "ID_3650EDAA-3095-4578-8E35-FF844099F895"),
                        M30.el("Kode")
                            .attr("V", expectedExchangeGroup.getReference())
                            .attr("DN", expectedExchangeGroup.getDisplay()),
                        M30.el("MerknadTilByttbarhet", "false")));

        Reference group = importer.readMedicationGroup(M30.el("KatByttegruppe", oppfByttegruppe)).get(0);
        assertThat(group).isEqualToComparingFieldByField(expectedExchangeGroup);
    }

    @Test
    public void readMedicationPackage() {
        /*
         *
        <PrisVare>
          <Type V="5" S="2.16.578.1.12.4.1.1.7453" DN="Trinnpris" />
          <Pris V="87.9" U="NOK" />
          <GyldigFraDato>2016-01-01</GyldigFraDato>
        </PrisVare>

         */

        Element oppfLegemiddelPakning =
                oppfWrapper(F.el("Legemiddelpakning",
                        F.el("Atc").attr("V", "A12AX").attr("S", "2.16.578.1.12.4.1.1.7180").attr("DN", "Kalsium..."),
                        F.el("NavnFormStyrke", "Calcigran Forte Tyggetab 1000 mg/800 IE"),
                        F.el("Reseptgruppe"),
                        F.el("PakningByttegruppe", F.el("RefByttegruppe", "ID_D1038B4D-A53B-414B-B51D-05D7D29D9A7C")),
                        F.el("Varenr", "170227"),
                        F.el("PrisVare",
                                F.el("Type").attr("V", "5").attr("S", "2.16.578.1.12.4.1.1.7453").attr("DN", "Trinnpris"),
                                F.el("Pris").attr("V", "87.9").attr("U", "NOK"))));
        validator.validate(M30.el("FEST",
                M30.el("HentetDato", Instant.now().toString()),
                M30.el("KatLegemiddelpakning", oppfLegemiddelPakning),
                M30.el("KatByttegruppe",
                        M30.el("OppfByttegruppe",
                                M30.el("Id", "test"),
                                M30.el("Tidspunkt", Instant.now().toString()),
                                M30.el("Status"),
                                M30.el("Byttegruppe",
                                        M30.el("Id", "ID_D1038B4D-A53B-414B-B51D-05D7D29D9A7C"),
                                        M30.el("Kode")
                                            .attr("V", "A")
                                            .attr("DN", "Nothing"),
                                        M30.el("MerknadTilByttbarhet", "false"))))));

        Medication medication = importer.readMedicationPackages(M30.el("KatLegemiddelpakning", oppfLegemiddelPakning)).get(0);
        assertThat(medication).hasNoNullFieldsOrProperties();

        assertThat(medication.getDisplay())
            .isEqualTo("Calcigran Forte Tyggetab 1000 mg/800 IE");
        assertThat(medication.getProductId())
            .isEqualTo("170227");
        assertThat(medication.getSubstitutionGroup())
            .isEqualTo("ID_D1038B4D-A53B-414B-B51D-05D7D29D9A7C");
    }


    @Test
    public void shouldReadInteractions() {
        Element oppInteraksjon = oppfWrapper(M30.el("Interaksjon",
                M30.el("Id", "ID_06688DFC-BF07-4113-A6E4-9F8F00E5A536"),
                M30.el("Relevans").attr("V", "1").attr("DN", "Bør unngås"),
                M30.el("KliniskKonsekvens", "Risiko for toksiske..."),
                M30.el("Interaksjonsmekanisme", "Metylfenidat frigjør..."),
                M30.el("Kildegrunnlag").attr("V", "4").attr("DN", "Indirekte data"),
                M30.el("Substansgruppe",
                        M30.el("Substans",
                                M30.el("Substans", "Metylfenidat"),
                                M30.el("Atc").attr("V", "N06BA05").attr("DN", "Metylfenidat"))),
                M30.el("Substansgruppe",
                        M30.el("Substans",
                                M30.el("Substans", "Moklobemid"),
                                M30.el("Atc").attr("V", "N06AG02").attr("DN", "Moklobemid")))
                ));
        validator.validate(M30.el("KatInteraksjon", oppInteraksjon));

        MedicationInteraction interaction = importer.readInteractions(M30.el("KatInteraksjon", oppInteraksjon)).get(0);

        assertThat(interaction).hasNoNullFieldsOrProperties();
        assertThat(interaction.getSubstanceCodes()).contains("N06BA05", "N06AG02");
        assertThat(interaction.getId()).isEqualTo("ID_06688DFC-BF07-4113-A6E4-9F8F00E5A536");
        assertThat(interaction.getSeverity()).isEqualTo(MedicalInteractionSeverity.SEVERE);
        assertThat(interaction.getClinicalConsequence()).isEqualTo("Risiko for toksiske...");
        assertThat(interaction.getInteractionMechanism()).isEqualTo("Metylfenidat frigjør...");
    }

    @Test
    public void shouldSkipInteractionsOnVirkestoff() {
        Element oppInteraksjon = oppfWrapper(M30.el("Interaksjon",
                M30.el("Id", "ID_22B74A78-B075-4B78-9CB4-9F9400FF377F"),
                M30.el("Relevans").attr("V", "3").attr("DN", "Ingen tiltak nødvendig"),
                M30.el("KliniskKonsekvens", "Økt konsentrasjon av triazolam..."),
                M30.el("Interaksjonsmekanisme", "Grapefruktjuice hemmer..."),
                M30.el("Kildegrunnlag").attr("V", "1").attr("DN", "Interaksjonsstudier"),
                M30.el("Substansgruppe",
                        M30.el("Navn", "Grapefruktjuice"),
                        M30.el("Substans",
                                M30.el("Substans", "(ikke angitt)"),
                                M30.el("RefVirkestoff", "ID_48C0F789"))),
                M30.el("Substansgruppe",
                        M30.el("Substans",
                                M30.el("Substans", "Triazolam"),
                                M30.el("Atc").attr("V", "N05CD05").attr("DN", "Triazolam")))
                ));
        validator.validate(M30.el("FEST",
                M30.el("HentetDato", Instant.now().toString()),
                M30.el("KatVirkestoff", oppfWrapper(F.el("Virkestoff",
                        F.el("Id", "ID_48C0F789"),
                        F.el("Navn", "(ikke angitt)")))),
                M30.el("KatInteraksjon", oppInteraksjon)));

        assertThat(importer.readInteractions(M30.el("KatInteraksjon", oppInteraksjon))).isEmpty();
    }

    private Element oppfWrapper(Element el) {
        return M30.el("Oppf" + el.tagName(),
                M30.el("Id", "ID_" + random.nextInt()),
                M30.el("Tidspunkt", Instant.now().toString()),
                M30.el("Status", ""),
                el);
    }

    public static void main(String[] args) {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testFest", "sa", "");

        Flyway flyway  = new Flyway();
        flyway.setLocations("db/db-medications");
        flyway.setDataSource(dataSource);
        flyway.migrate();

        JdbcMedicationRepository repository = new JdbcMedicationRepository(dataSource);
        repository.refresh();
    }
}
