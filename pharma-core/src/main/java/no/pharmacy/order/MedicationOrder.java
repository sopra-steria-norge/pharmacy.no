package no.pharmacy.order;

import java.time.LocalDate;
import java.util.UUID;

import org.eaxy.Document;
import org.eaxy.Validator;
import org.eaxy.Xml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.medication.Medication;

@ToString
public class MedicationOrder {

    @Getter @Setter
    private String identifier;

    @Getter @Setter
    private Reference prescriber;
    private LocalDate dateWritten;

    @Getter @Setter
    private Medication medication;

    public MedicationOrder() {
        // TODO Auto-generated constructor stub
    }

    public MedicationOrder(Document d) {
//        this.identifier;
//        this.prescriber;
//        this.medication;
        Validator validator = Xml.validatorFromResource("R0908-eResept-M1-M21-2013-10-08/ER-M1-2013-10-08.xsd");
        validator.validate(d.getRootElement());

        this.identifier = "hello world";
        this.prescriber = new Reference("test", "test display");
        this.medication = new Medication("1243", "hello", 1000, 1000);
        // TODO Auto-generated constructor stub
    }

    public MedicationOrder(String nationalId, Medication product) {
        this.medication = product;
        this.identifier = UUID.randomUUID().toString();
    }

    public void setDateWritten(LocalDate dateWritten) {
        this.dateWritten = dateWritten;
    }

    public String getRefundGroup() {
        return dateWritten.toString() + "/" + prescriber.getReference();
    }



}
