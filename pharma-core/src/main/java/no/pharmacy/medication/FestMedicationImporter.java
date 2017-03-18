package no.pharmacy.medication;

import java.util.ArrayList;
import java.util.List;

import org.eaxy.Element;
import org.eaxy.Validator;

import no.pharmacy.order.Reference;

public class FestMedicationImporter {
    private Validator validator = new Validator(new String[] { "R1808-eResept-M30-2014-12-01/ER-M30-2014-12-01.xsd" });


    public List<Reference> readMedicationGroup(Element oppfByttegruppe) {
        validator.validate(oppfByttegruppe);

        List<Reference> result = new ArrayList<>();
        for (Element byttegruppe : oppfByttegruppe.elements()) {
            Element byttegruppeKode = byttegruppe.find("Byttegruppe", "Kode").first();
            result.add(new Reference(byttegruppeKode.attr("V"), byttegruppeKode.attr("DN")));
        }

        return result;
    }


    public List<Medication> readMedicationPackage(Element katLegemiddelpakning) { List<Medication> result = new ArrayList<>();
        for (Element oppfLegemiddelpakning : katLegemiddelpakning.elements()) {
            Element legemiddelpakning = oppfLegemiddelpakning.find("Legemiddelpakning").first();
            Medication medication = new Medication();
            medication.setDisplay(legemiddelpakning.find("NavnFormStyrke").first().text());
            medication.setProductId(legemiddelpakning.find("Varenr").first().text());
            medication.setSubstitutionGroup(legemiddelpakning.find("PakningByttegruppe", "RefByttegruppe").firstTextOrNull());
            medication.setXml(legemiddelpakning.toXML());
            result.add(medication);
        }

        return result;
    }

}
