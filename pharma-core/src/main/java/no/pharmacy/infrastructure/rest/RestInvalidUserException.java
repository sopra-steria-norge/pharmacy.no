package no.pharmacy.infrastructure.rest;

import java.io.IOException;
import java.net.HttpURLConnection;

public class RestInvalidUserException extends RestHttpException {

    public RestInvalidUserException(HttpURLConnection connection) throws IOException {
        super(connection);
    }

}
