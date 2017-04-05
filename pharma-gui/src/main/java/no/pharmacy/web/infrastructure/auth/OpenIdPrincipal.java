package no.pharmacy.web.infrastructure.auth;

import java.security.Principal;

import javax.security.auth.Subject;

import no.pharmacy.infrastructure.auth.JwtToken;

public class OpenIdPrincipal implements Principal {

    private String name;
    private String subject;
    private JwtToken jwt;

    public OpenIdPrincipal(JwtToken jwt) {
        this.jwt = jwt;
        setSubject(jwt.sub());
        setName(jwt.name().orElse(jwt.sub()));
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Subject getSubject() {
        return null;
    }

    public String[] getRoles() {
        return new String[0];
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public JwtToken getToken() {
        return jwt;
    }
}