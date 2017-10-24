package no.pharmacy.test;

import java.util.ArrayList;
import java.util.List;

import org.eaxy.Element;

import no.pharmacy.infrastructure.messages.EbXmlMessage;

public class PrescriptionMessages {

    private List<EbXmlMessage> dispenseMessages = new ArrayList<>();

    public EbXmlMessage singleDispenseMessage() {
        if (dispenseMessages.size() != 1) {
            throw new IllegalStateException("dispenseMessages.size = " + dispenseMessages.size() + " != 1");
        }
        return dispenseMessages.get(0);
    }

    public void addDispense(Element message) {
        this.dispenseMessages.add(new EbXmlMessage(message));
    }

}
