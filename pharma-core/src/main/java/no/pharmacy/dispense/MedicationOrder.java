package no.pharmacy.dispense;

import java.time.LocalDate;
import java.util.List;
import org.eaxy.Document;
import org.eaxy.Validator;
import org.eaxy.Xml;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.Reference;
import no.pharmacy.medication.Medication;
import no.pharmacy.medicationorder.MedicationOrderSummary;

@EqualsAndHashCode(of={"id", "prescriber", "dateWritten", "medication"}, callSuper=true)
@ToString
public class MedicationOrder extends MedicationOrderSummary {

    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String prescriptionId;

    @Getter @Setter
    private Reference prescriber;

    @Getter @Setter
    private LocalDate dateWritten;

    @Getter
    private Medication medication;

    @Getter @Setter
    private List<Medication> alternatives;

    @Getter @Setter
    private String dosageText;

    public MedicationOrder() {
        // TODO Auto-generated constructor stub
    }

    MedicationOrder(Document d) {
//        this.identifier;
//        this.prescriber;
//        this.medication;
        Validator validator = Xml.validatorFromResource("R0908-eResept-M1-M21-2013-10-08/ER-M1-2013-10-08.xsd");
        validator.validate(d.getRootElement());

        this.prescriber = new Reference("test", "test display");
        this.medication = new Medication("1243", "hello", 1000);
        // TODO Auto-generated constructor stub
    }

    public MedicationOrder(Medication product) {
        this.medication = product;
        setMedicationName(product.getDisplay());
    }

    public String getRefundGroup() {
        return dateWritten + "/" + prescriber.getReference();
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
        setMedicationName(medication.getDisplay());
    }
}
