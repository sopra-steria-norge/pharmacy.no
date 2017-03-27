package no.pharmacy.web.dispense;

import java.io.IOException;

import org.eaxy.Document;

public interface HtmlView {

    Document createView() throws IOException;

}
