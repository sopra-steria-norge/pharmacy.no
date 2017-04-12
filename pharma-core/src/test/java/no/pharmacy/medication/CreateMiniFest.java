package no.pharmacy.medication;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Namespace;
import org.eaxy.Validator;
import org.eaxy.Xml;

public class CreateMiniFest {

    private static Random random = new Random();

    private static final Namespace M30 = new Namespace("http://www.kith.no/xmlstds/eresept/m30/2014-12-01", "m30");
    private static final Validator validator = Xml.validatorFromResource("R1808-eResept-M30-2014-12-01/ER-M30-2014-12-01.xsd");

    private Element katLegemiddelpakning = M30.el("KatLegemiddelpakning");
    private Element katRefusjon = M30.el("KatRefusjon");
    private Element katLegemiddelMerkevare = M30.el("KatLegemiddelMerkevare");
    private Element katVirkestoff = M30.el("KatVirkestoff");
    private Element katLegemiddelVirkestoff = M30.el("KatLegemiddelVirkestoff");
    private Element katVilkar = M30.el("KatVilkar");
    private Element katVarsler = M30.el("KatVarselSlv");
    private Element katByttegruppe = M30.el("KatByttegruppe");
    private Element katInteraksjon = M30.el("KatInteraksjon");
    private Set<String> includedIds = new HashSet<>();

    private Set<String> includedAtcCodes = new HashSet<>();

    private Document festDoc;

    public CreateMiniFest(Document festDoc) {
        this.festDoc = festDoc;
    }

    private Document extractMiniFest(int count) {
        List<Element> pakninger = new ArrayList<>(festDoc.getRootElement().find("KatLegemiddelpakning").first().elements());

        for (int i=0; i<count; i++) {
            Element pakning = pakninger.get(random.nextInt(pakninger.size()));
            includeLegemiddelpakning(pakning.find("Legemiddelpakning", "Id").first().text());
        }

        String[] varenrMedByttegruppe = {
                "038397", "048788", "048797", "089497", "438175", "010965", "022113", "183039", "458492", "088656", "043175", "023949", "164606", "587209", "397520", "414533", "004752", "011054", "578081", "004741", "038407", "081825", "159558", "071712", "164617", "114740", "022175", "079261", "423739", "038507", "010288", "088645", "435610", "500038", "164628", "403219", "595422", "384157", "509091", "184839"
        };
        for (String id : varenrMedByttegruppe) {
            includeLegemiddelpakningByVarenummer(id);
        }

        includeLegemiddelpakning("ID_5DC7F4A6-EB4D-48DC-93B7-4A4B1D0573CC"); // Hepsera Tab 10 mg
        includeLegemiddelpakning("ID_0819AD27-301D-4A53-8ED6-CB46F0A67770"); // Paracetduo
        includeLegemiddelpakning("ID_29E9684D-3D1D-4C5C-8663-A478297892CD"); // NovoRapid Flexpen
        includeLegemiddelpakning("ID_99C4A44A-0968-48C2-8D59-51F93457EBC7"); // Ritalin
        includeLegemiddelpakning("ID_6312E47E-4874-461B-AA4A-0C27F134A5A8"); // Aurorix - Moklobemid

        includeVarsler();

        includeInteraksjoner();

        return Xml.doc(
                M30.el("FEST",
                        M30.el("HentetDato", Instant.now().toString()),
                        katLegemiddelMerkevare,
                        katLegemiddelpakning,
                        katVirkestoff,
                        katLegemiddelVirkestoff,
                        katRefusjon,
                        katVilkar,
                        katVarsler,
                        katByttegruppe,
                        katInteraksjon
                        )
                );
    }

    private void includeLegemiddelpakningByVarenummer(String varenummer) {
        for (Element oppfLegemiddelpakning : festDoc.find("KatLegemiddelpakning", "OppfLegemiddelpakning")) {
            if (varenummer.equals(oppfLegemiddelpakning.find("Legemiddelpakning", "Varenr").first().text())) {
                includeLegemiddelpakning(oppfLegemiddelpakning);
                return;
            }
        }
        throw new IllegalArgumentException("Can't find OppfLegemiddelpakning with Legemiddelpakning > Varenr " + varenummer);
    }

    private void includeInteraksjoner() {
        outer: for (Element oppfInteraksjon : festDoc.find("KatInteraksjon", "OppfInteraksjon")) {
            ElementSet substansgrupper = oppfInteraksjon.find("Interaksjon", "Substansgruppe");
            if (substansgrupper.isEmpty()) continue;
            for (Element substansgruppe : substansgrupper) {
                if (Collections.disjoint(substansgruppe.find("Substans", "Atc").attrs("V"), includedAtcCodes)) {
                    continue outer;
                }
            }
            katInteraksjon.add(oppfInteraksjon);
            includedIds.add(oppfInteraksjon.find("Interaksjon", "Id").first().text());
            includeVirkestoff(oppfInteraksjon.find("Interaksjon", "Substansgruppe", "Substans").check().find("RefVirkestoff").texts());
        }
    }

    private void includeVarsler() {
        outer: for (Element oppfVarsel : festDoc.find("KatVarselSlv", "OppfVarselSlv")) {
            for (Element referanseelement : oppfVarsel.find("VarselSlv", "Referanseelement")) {
                String referenceClass = referanseelement.find("Klasse").first().attr("DN");

                ElementSet refElements = referanseelement.find("RefElement");
                if (refElements.size() > 10) {
                    System.out.println("Skipping large RefElement for " + oppfVarsel.find("VarselSlv", "Overskrift").first().text());
                    continue outer;
                }

                for (Element refElement : refElements) {
                    if (referenceClass.equals("LegemiddelpakningId")) {
                        includeLegemiddelpakning(refElement.text());
                    } else if (referenceClass.equals("LegemiddelMerkevareId")) {
                        includeMerkevare(refElement.text());
                    } else if (referenceClass.equals("LegemiddelVirkestoffId")) {
                        includeLegemiddelVirkestoff(refElement.text());
                    } else {
                        throw new IllegalArgumentException("Unknown Klasse " + referenceClass);
                    }
                }
            }

            katVarsler.add(oppfVarsel);
        }

    }

    private void includeLegemiddelpakning(String id) {
        if (!includedIds.contains(id)) {
            for (Element oppfLegemiddelpakning : festDoc.find("KatLegemiddelpakning", "OppfLegemiddelpakning")) {
                if (id.equals(oppfLegemiddelpakning.find("Legemiddelpakning", "Id").first().text())) {
                    includeLegemiddelpakning(oppfLegemiddelpakning);
                    return;
                }
            }
            throw new IllegalArgumentException("Can't find OppfLegemiddelpakning with Legemiddelpakning > Id " + id);
        }
    }

    private void includeLegemiddelpakning(Element oppfLegemiddelpakning) {
        String id = oppfLegemiddelpakning.find("Legemiddelpakning", "Id").first().text();
        if (includedIds.contains(id)) return;

        katLegemiddelpakning.add(oppfLegemiddelpakning);
        includedIds.add(id);
        includeAtcCodes(oppfLegemiddelpakning.find("Legemiddelpakning", "Atc").attrs("V"));

        includeRefusjon(oppfLegemiddelpakning.find("Legemiddelpakning", "Refusjon", "RefRefusjonsgruppe").texts());
        includeByttegrupper(oppfLegemiddelpakning.find("Legemiddelpakning", "PakningByttegruppe", "RefByttegruppe").texts());
        includeMerkevarer(oppfLegemiddelpakning.find("Legemiddelpakning", "Pakningsinfo", "RefLegemiddelMerkevare").texts());
    }

    private void includeAtcCodes(List<String> atcCodes) {
        for (String atcCode : atcCodes) {
            includedAtcCodes.add(atcCode.substring(0, 1));
            includedAtcCodes.add(atcCode.substring(0, 3));
            includedAtcCodes.add(atcCode.substring(0, 4));
            includedAtcCodes.add(atcCode.substring(0, 5));
            includedAtcCodes.add(atcCode);
        }
    }

    private void includeMerkevarer(List<String> merkevareIds) {
        for (String id : merkevareIds) {
            includeMerkevare(id);
        }
    }

    private void includeMerkevare(String id) {
        if (!includedIds.contains(id)) {
            for (Element oppfLegemiddelMerkevare : festDoc.find("KatLegemiddelMerkevare", "OppfLegemiddelMerkevare")) {
                if (oppfLegemiddelMerkevare.find("LegemiddelMerkevare", "Id").firstTextOrNull().equals(id)) {
                    katLegemiddelMerkevare.add(oppfLegemiddelMerkevare);
                    includeAtcCodes(oppfLegemiddelMerkevare.find("LegemiddelMerkevare", "Atc").attrs("V"));

                    includeVirkestoffMedStyrke(oppfLegemiddelMerkevare.find("LegemiddelMerkevare", "SortertVirkestoffMedStyrke", "RefVirkestoffMedStyrke").texts());
                    includeVirkestoff(oppfLegemiddelMerkevare.find("LegemiddelMerkevare", "SortertVirkestoffUtenStyrke", "RefVirkestoff").texts());
                    includeVilkar(oppfLegemiddelMerkevare.find("LegemiddelMerkevare", "RefVilkar").texts());

                    includedIds.add(id);
                    return;
                }
            }
            throw new IllegalArgumentException("Can't find LegemiddelMerkevare Id " + id);
        }
    }

    private void includeByttegrupper(List<String> byttegruppeIds) {
        outer: for (String byttegruppeId : byttegruppeIds) {
            if (!includedIds.contains(byttegruppeId)) {
                for (Element oppfByttegruppe : festDoc.find("KatByttegruppe", "OppfByttegruppe")) {
                    if (oppfByttegruppe.find("Byttegruppe", "Id").firstTextOrNull().equals(byttegruppeId)) {
                        katByttegruppe.add(oppfByttegruppe);
                        includedIds.add(byttegruppeId);

                        includeLegemiddelpakningInByttegruppe(byttegruppeId);

                        continue outer;
                    }
                }
                throw new IllegalArgumentException("Can't find Byttegruppe Id " + byttegruppeId);
            }
        }
    }

    private void includeLegemiddelpakningInByttegruppe(String byttegruppeId) {
        for (Element oppfLegemiddelpakning : festDoc.find("KatLegemiddelpakning", "OppfLegemiddelpakning")) {
            if (oppfLegemiddelpakning.find("Legemiddelpakning", "PakningByttegruppe", "RefByttegruppe").texts().contains(byttegruppeId)) {
                includeLegemiddelpakning(oppfLegemiddelpakning);
            }
        }
    }

    private void includeRefusjon(List<String> refusjonsIds) {
        outer: for (String refusjonsId : refusjonsIds) {
            if (!includedIds.contains(refusjonsId)) {
                for (Element oppfRefusjonsgruppe : festDoc.find("KatRefusjon", "OppfRefusjon")) {
                    if (oppfRefusjonsgruppe.find("Refusjonshjemmel", "Refusjonsgruppe", "Id").firstTextOrNull().equals(refusjonsId)) {
                        katRefusjon.add(oppfRefusjonsgruppe);
                        includeAtcCodes(oppfRefusjonsgruppe.find("Refusjonshjemmel", "Refusjonsgruppe", "Atc").attrs("V"));

                        includeVilkar(oppfRefusjonsgruppe.find("Refusjonshjemmel", "Refusjonsgruppe", "Refusjonskode", "Refusjonsvilkar", "RefVilkar").texts());
                        includeVilkar(oppfRefusjonsgruppe.find("Refusjonshjemmel", "Refusjonsgruppe", "RefVilkar").texts());
                        includedIds.add(refusjonsId);
                        continue outer;
                    }
                }
                throw new IllegalArgumentException("Can't find Refusjonshjemmel Refusjonsgruppe Id " + refusjonsId);
            }
        }
    }

    private void includeVilkar(List<String> vilkarIds) {
        outer: for (String vilkarId : vilkarIds) {
            if (!includedIds.contains(vilkarId)) {
                for (Element oppfVilkar : festDoc.find("KatVilkar", "OppfVilkar")) {
                    if (vilkarId.equals(oppfVilkar.find("Vilkar", "Id").firstTextOrNull())) {
                        katVilkar.add(oppfVilkar);
                        includedIds.add(vilkarId);
                        continue outer;
                    }
                }
                throw new IllegalArgumentException("Can't find Vilkar Id " + vilkarId);
            }
        }
    }

    private void includeVirkestoffMedStyrke(List<String> virkestoffIds) {
        outer: for (String virkestoffId : virkestoffIds) {
            if (!includedIds.contains(virkestoffId)) {
                for (Element oppfVirkestoff : festDoc.find("KatVirkestoff", "OppfVirkestoff")) {
                    if (virkestoffId.equals(oppfVirkestoff.find("VirkestoffMedStyrke", "Id").firstTextOrNull())) {
                        katVirkestoff.add(oppfVirkestoff);
                        includedIds.add(virkestoffId);
                        includeAtcCodes(oppfVirkestoff.find("VirkestoffMedStyrke", "AtcKombipreparat").attrs("V"));
                        includeVirkestoff(oppfVirkestoff.find("VirkestoffMedStyrke", "RefVirkestoff").texts());
                        continue outer;
                    }
                }
                throw new IllegalArgumentException("Can't find VirkestoffMedStyrke Id " + virkestoffId);
            }
        }
    }

    private void includeVirkestoff(List<String> virkestoffIds) {
        for (String virkestoffId : virkestoffIds) {
            includeVirkestoff(virkestoffId);
        }
    }

    private void includeVirkestoff(String virkestoffId) {
        if (!includedIds.contains(virkestoffId)) {
            for (Element oppfVirkestoff : festDoc.find("KatVirkestoff", "OppfVirkestoff")) {
                if (virkestoffId.equals(oppfVirkestoff.find("Virkestoff", "Id").firstTextOrNull())) {
                    katVirkestoff.add(oppfVirkestoff);
                    includedIds.add(virkestoffId);
                    includeVirkestoff(oppfVirkestoff.find("Virkestoff", "RefVirkestoff").texts());
                    return;
                }
            }
            throw new IllegalArgumentException("Can't find Virkestoff Id " + virkestoffId);
        }
    }

    private void includeLegemiddelVirkestoff(String virkestoffId) {
        if (!includedIds.contains(virkestoffId)) {
            for (Element oppfLegemiddelVirkestoff : festDoc.find("KatLegemiddelVirkestoff", "OppfLegemiddelVirkestoff").check()) {
                if (virkestoffId.equals(oppfLegemiddelVirkestoff.find("LegemiddelVirkestoff", "Id").first().text())) {
                    katLegemiddelVirkestoff.add(oppfLegemiddelVirkestoff);
                    includedIds.add(virkestoffId);
                    includeVirkestoffMedStyrke(oppfLegemiddelVirkestoff.find("LegemiddelVirkestoff", "SortertVirkestoffMedStyrke", "RefVirkestoffMedStyrke").texts());
                    return;
                }
            }
            throw new IllegalArgumentException("Can't find OppfLegemiddelVirkestoff Id " + virkestoffId);
        }
    }

    public static void main(String[] args) throws IOException {
        Document festDoc = FestMedicationImporter.downloadFestDoc(FestMedicationImporter.FEST_URL);
        Document miniFest = new CreateMiniFest(festDoc).extractMiniFest(10);
        System.out.println("Extract complete");

        try (Writer writer = new FileWriter("fest-mini.xml")) {
            miniFest.writeTo(writer);
        }
        try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("fest-mini.xml.gz")), StandardCharsets.UTF_8)) {
            miniFest.writeTo(writer);
        }
        System.out.println("Write complete");
        validator.validate(miniFest.getRootElement());
    }

}
