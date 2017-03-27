package no.pharmacy.infrastructure.rest;

import java.net.URL;

public class RestParseException extends RestException {
    public RestParseException(Exception e, URL url) {
        super(e, url);
    }
}
