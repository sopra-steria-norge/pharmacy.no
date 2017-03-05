package no.pharmacy.infrastructure;

import java.net.URL;

public class RestParseException extends RestException {
    public RestParseException(Exception e, URL url) {
        super(e, url);
    }
}
