package no.pharmacy.core;

import org.eaxy.Element;

public interface MessageGateway {

    Element processRequest(Element request);

}
