package no.pharmacy.medication;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.eaxy.Element;
import org.eaxy.Namespace;
import org.junit.Test;

import no.pharmacy.order.Reference;

public class FestMedicationImporterTest {

    private static final Namespace M30 = new Namespace("http://www.kith.no/xmlstds/eresept/m30/2014-12-01", "m30");
    private static final Namespace F = new Namespace("http://www.kith.no/xmlstds/eresept/forskrivning/2014-12-01", "f");
    private FestMedicationImporter importer = new FestMedicationImporter();


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
        Element oppfLegemiddelPakning = M30.el("OppfLegemiddelpakning",
                M30.el("Id", "dgsdg"),
                M30.el("Tidspunkt", Instant.now().toString()),
                M30.el("Status", ""),
                F.el("Legemiddelpakning",
                        F.el("NavnFormStyrke", "Calcigran Forte Tyggetab 1000 mg/800 IE"),
                        F.el("Reseptgruppe"),
                        F.el("PakningByttegruppe", F.el("RefByttegruppe", "ID_D1038B4D-A53B-414B-B51D-05D7D29D9A7C")),
                        F.el("Varenr", "170227"))
                );

        Medication medication = importer.readMedicationPackage(M30.el("KatLegemiddelpakning", oppfLegemiddelPakning)).get(0);
        assertThat(medication)
            .hasNoNullFieldsOrPropertiesExcept("trinnPrice", "retailPrice");

        assertThat(medication.getDisplay())
            .isEqualTo("Calcigran Forte Tyggetab 1000 mg/800 IE");
        assertThat(medication.getProductId())
            .isEqualTo("170227");
        assertThat(medication.getSubstitutionGroup())
            .isEqualTo("ID_D1038B4D-A53B-414B-B51D-05D7D29D9A7C");
    }

}
