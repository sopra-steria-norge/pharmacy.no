package no.pharmacy.infrastructure.dsig;

import org.eaxy.Document;

public interface Canonization {

    String canonize(Document doc, String... path);

}
