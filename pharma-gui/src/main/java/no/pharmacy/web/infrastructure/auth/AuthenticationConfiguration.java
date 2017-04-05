package no.pharmacy.web.infrastructure.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.infrastructure.auth.JwtToken;

public class AuthenticationConfiguration {

    private String authority;
    private String tenant;
    private String clientId;

    public AuthenticationConfiguration() {
        List<String> missingFields = new ArrayList<>();

        tenant = getEnv("AD_TENANT", missingFields);
        clientId = getEnv("AD_CLIENT_ID", missingFields);
        authority = getEnv("AD_AUTHORITY", missingFields);

        if (!missingFields.isEmpty()) {
            throw new RuntimeException("Can't start without Active Directory ENVIRONMENT VARIABLES: " + missingFields);
        }
    }

    private static String getEnv(String name, List<String> missingFields) {
        if (System.getenv(name) == null) {
            missingFields.add(name);
            return null;
        }
        return System.getenv(name);
    }

    public String getRedirectUrl(HttpServletRequest req) {
        return this.authority + this.tenant
                + "/oauth2/authorize?response_type=code%20id_token&scope=openid&response_mode=form_post&"
                + "redirect_uri=" + encodeUTF8(getCurrentUri(req)) + "&client_id=" + clientId
                + "&resource=https%3a%2f%2fgraph.windows.net"
                + "&nonce=" + UUID.randomUUID();
    }

    private static String encodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    public String getCurrentUri(HttpServletRequest req) {
        return getRequestUrl(req) + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
    }

    private static String getRequestUrl(HttpServletRequest req) {
        return getAuthority(req) + req.getRequestURI();
    }

    private static String getAuthority(HttpServletRequest req) {
        int port = req.getServerPort();
        boolean isDefaultPort = isSecure(req) && port == 443 || port == 80;
        return (isSecure(req) ? "https" : "http") + "://" + req.getServerName() + (isDefaultPort ? "" : ":" + port);
    }

    private static boolean isSecure(HttpServletRequest req) {
        return "https".equals(req.getScheme()) || req.getServerName().contains(".azurewebsites.net");
    }

    public OpenIdPrincipal decodeUserPrincipal(String idToken) {
        JwtToken jwt = new JwtToken(idToken);
        if (!jwt.isValid()) {
            return null;
        }
        return new OpenIdPrincipal(jwt);
    }
}
