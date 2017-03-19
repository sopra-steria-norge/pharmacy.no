package no.pharmacy.order;

import java.time.LocalDate;
import java.util.List;
import org.eaxy.Document;
import org.eaxy.Validator;
import org.eaxy.Xml;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.medication.Medication;

@EqualsAndHashCode(of={"id", "prescriber", "dateWritten", "medication"})
@ToString
public class MedicationOrder {

    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String prescriptionId;

    @Getter @Setter
    private Reference prescriber;

    @Getter @Setter
    private LocalDate dateWritten;

    @Getter @Setter
    private Medication medication;

    @Getter @Setter
    private List<Medication> alternatives;

    public MedicationOrder() {
        // TODO Auto-generated constructor stub
    }

    public MedicationOrder(Document d) {
//        this.identifier;
//        this.prescriber;
//        this.medication;
        Validator validator = Xml.validatorFromResource("R0908-eResept-M1-M21-2013-10-08/ER-M1-2013-10-08.xsd");
        validator.validate(d.getRootElement());

        this.prescriber = new Reference("test", "test display");
        this.medication = new Medication("1243", "hello", 1000, 1000);
        // TODO Auto-generated constructor stub
    }

    public MedicationOrder(String nationalId, Medication product) {
        this.medication = product;
    }

    public String getRefundGroup() {
        return dateWritten + "/" + prescriber.getReference();
    }
}
