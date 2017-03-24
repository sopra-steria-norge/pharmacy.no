package no.pharmacy.medication;

import java.util.ArrayList;
import java.util.List;

import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Validator;

import no.pharmacy.core.Money;
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


    public List<Medication> readMedicationPackages(Element katLegemiddelpakning) { List<Medication> result = new ArrayList<>();
        for (Element oppfLegemiddelpakning : katLegemiddelpakning.elements()) {
            Element legemiddelpakning = oppfLegemiddelpakning.find("Legemiddelpakning").first();
            Medication medication = new Medication();
            medication.setDisplay(legemiddelpakning.find("NavnFormStyrke").first().text());
            medication.setProductId(legemiddelpakning.find("Varenr").first().text());
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


    public List<MedicationInteraction> readInteractions(Element katInteraksjon) {
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

}
