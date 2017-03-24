package no.pharmacy.web.prescriptions;

import java.io.IOException;

import org.eaxy.Document;

public interface HtmlView {

    Document createView() throws IOException;

}
