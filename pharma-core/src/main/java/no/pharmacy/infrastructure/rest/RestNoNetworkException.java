package no.pharmacy.infrastructure.rest;

import java.net.URL;

public class RestNoNetworkException extends RestException {

    private String messageToUser;

    public RestNoNetworkException(String message, String messageToUser, URL url) {
        super(message, url);
        this.messageToUser = messageToUser;
    }

    public String getMessageToUser() {
        return messageToUser;
    }
}
