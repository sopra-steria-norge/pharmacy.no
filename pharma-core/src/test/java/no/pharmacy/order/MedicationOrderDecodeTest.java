package no.pharmacy.order;

import java.time.Instant;
import java.time.LocalDate;

import org.eaxy.Document;
import org.eaxy.Namespace;
import org.eaxy.Xml;
import org.junit.Test;

import no.pharmacy.medication.Medication;
import no.pharmacy.test.FakeMedicationSource;
import no.pharmacy.test.PharmaTestData;

public class MedicationOrderDecodeTest {

    private static Namespace ERESEPT = new Namespace("http://www.kith.no/xmlstds/eresept/m1/2013-10-08", "er");
    private static Namespace FORSKRIVNING = new Namespace("http://www.kith.no/xmlstds/eresept/forskrivning/2013-10-08", "f");


    @Test
    public void decodesM1() throws Exception {
        PharmaTestData testData = new PharmaTestData();
        Medication medication = testData.sampleMedication(new FakeMedicationSource());
        Document prescriptionDocument = Xml.doc(ERESEPT.el("Resept",
                ERESEPT.el("Forskrivningsdato", LocalDate.now().toString()),
                ERESEPT.el("Utloper", LocalDate.now().plusDays(14).toString()),
                ERESEPT.el("ReseptDokLegemiddel",
                        ERESEPT.el("Varegruppekode"),
                        ERESEPT.el("Reiterasjon", "0"),
                        FORSKRIVNING.el("Forskrivning",
                                FORSKRIVNING.el("Legemiddelpakning",
                                        FORSKRIVNING.el("NavnFormStyrke", medication.getDisplay()),
                                        FORSKRIVNING.el("Reseptgruppe", ""),
                                        FORSKRIVNING.el("Varenr", medication.getProductId())))
                        ),
                ERESEPT.el("OppdatertFest", Instant.now().toString())
                ));
        MedicationOrder order = new MedicationOrder(prescriptionDocument);

        // Is in the message envelope!
//        Assertions.assertThat(order.getPrescriber().getReference())
//            .isEqualTo(prescriber.getIdentifier());
//        Assertions.assertThat(order.getMedication())
//            .isEqualTo(medication);
    }


}
