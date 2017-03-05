package no.pharmacy.infrastructure;

import java.io.IOException;
import java.net.URL;

public class RestException extends IOException {
    private final URL url;

    protected RestException(String message, URL url) {
        super(message + " (" + url + ")");
        this.url = url;
    }

    protected RestException(Exception e, URL url) {
        super(e);
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

}
