package no.pharmacy.infrastructure;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RestHttpException extends RestException {

    private int responseCode;
    private String responseMessage;
    private String detailText;

    public RestHttpException(HttpURLConnection connection) throws IOException {
        this(connection.getResponseCode(), connection.getResponseMessage(), connection.getURL());
        if (connection.getErrorStream() != null) {
            this.detailText = IOUtil.toString(connection.getErrorStream(), StandardCharsets.UTF_8);
        }
    }

    public RestHttpException(int responseCode, String responseMessage, URL url) {
        super(responseCode + " " + responseMessage, url);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getDetailText() {
        return detailText;
    }

}