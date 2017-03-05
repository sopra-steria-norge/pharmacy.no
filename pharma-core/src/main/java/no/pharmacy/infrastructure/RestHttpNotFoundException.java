package no.pharmacy.infrastructure;

import java.io.IOException;
import java.net.HttpURLConnection;

public class RestHttpNotFoundException extends RestHttpException {

    public RestHttpNotFoundException(HttpURLConnection connection) throws IOException {
        super(connection);
    }

}
