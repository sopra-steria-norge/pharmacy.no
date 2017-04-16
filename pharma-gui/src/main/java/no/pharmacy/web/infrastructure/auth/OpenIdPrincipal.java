package no.pharmacy.web.infrastructure.auth;

import java.security.Principal;

import javax.security.auth.Subject;

import no.pharmacy.infrastructure.auth.JwtToken;

public class OpenIdPrincipal implements Principal {

    private JwtToken jwt;

    public OpenIdPrincipal(JwtToken jwt) {
        this.jwt = jwt;
    }

    @Override
    public String getName() {
        return jwt.getUserName();
    }

    public Subject getSubject() {
        return null;
    }

    public String[] getRoles() {
        return new String[0];
    }

    public JwtToken getToken() {
        return jwt;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name=" + getName() + ",token=" + getToken() + "}";
    }

    public String getDisplayName() {
        return jwt.getDisplayName();
    }
}
