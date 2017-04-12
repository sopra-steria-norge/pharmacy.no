package no.pharmacy.infrastructure.auth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.infrastructure.IOUtil;


public class JwtToken {

    private static final Logger logger = LoggerFactory.getLogger(JwtToken.class);

    private static CertificateFactory certificateFactory;

    private static Map<String,Certificate> certificates = new HashMap<>();
    private String[] tokenValues;
    private JsonObject jwtHeader;
    private JsonObject payload;

    public JwtToken(String idToken) {
        tokenValues = idToken.split("\\.");
        jwtHeader = parseBase64Json(tokenValues[0]);
        payload = parseBase64Json(tokenValues[1]);
    }

    public boolean isValid() {
        try {
            if (!verifySignature()) {
                logger.warn("Failed to signature");
                return false;
            } else {
                logger.debug("Verified signature");
            }
        } catch (GeneralSecurityException e) {
            logger.warn("Failed to validate token {}", e);
            return false;
        }
        return verifyTimeValidity(Instant.now());
    }

    public boolean verifyTimeValidity(Instant instant) {
        if (instant.isBefore(nbf().orElse(Instant.MIN))) {
            logger.warn("JWT not valid yet! " + payload);
            return false;
        }
        if (instant.isAfter(exp())) {
            logger.warn("JTW expired! " + payload);
            return false;
        }
        return true;
    }

    /**
     * Issuer. The iss value is a case sensitive URL using the https scheme that contains scheme,
     * host, and optionally, port number and path components and no query or fragment components.
     */
    public String iss() {
        return payload.requiredString("iss");
    }

    /**
     * Subject Identifier. A locally unique and never reassigned identifier within the Issuer for
     * the End-User, which is intended to be consumed by the Client
     */
    public String sub() {
        return payload.requiredString("sub");
    }

    /**
     * Audience(s) that this ID Token is intended for. It MUST contain the OAuth 2.0 client_id
     * of the Relying Party as an audience value. It MAY also contain identifiers for other
     * audiences. In the general case, the aud value is an array of case sensitive strings.
     * In the common special case when there is one audience, the aud value MAY be a single
     * case sensitive string.
     */
    public String aud() {
        return payload.requiredString("aud");
    }

    private JsonObject parseBase64Json(String base64encodedJson) {
        return JsonParser.parseToObject(new String(base64Decode(base64encodedJson)));
    }

    private Optional<Instant> nbf() {
        return payload.longValue("nbf").map(Instant::ofEpochSecond);
    }

    /**
     *  Expiration time on or after which the ID Token MUST NOT be accepted for processing.
     */
    public Instant exp() {
        return Instant.ofEpochSecond(payload.requiredLong("exp"));
    }

    /**
     *  Time at which the JWT was issued
     */
    public Instant iat() {
        return Instant.ofEpochSecond(payload.requiredLong("exp"));
    }

    /**
     * Authentication Methods References
     */
    public Optional<String> amr() {
        return claim("amr");
    }

    public Optional<String> name() {
        return claim("name");
    }


    public Optional<String> claim(String claimId) {
        return payload.stringValue(claimId);
    }

    public Set<String> claimNames() {
        return payload.keys();
    }

    public boolean verifySignature() throws GeneralSecurityException {
        if (!alg().equals("RS256")) {
            throw new IllegalArgumentException("Illegal algorithm " + alg());
        }
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(getCertificate().getPublicKey());
        signature.update((tokenValues[0] + "." + tokenValues[1]).getBytes());
        return signature.verify(base64Decode(tokenValues[2]));
    }

    private String alg() {
        return jwtHeader.requiredString("alg");
    }

    private Certificate getCertificate() throws CertificateException {
        if (iss().startsWith("https://sts.windows.net/")) {
            String tenant = payload.requiredString("tid");
            String keyUrl = "https://login.microsoftonline.com/" + tenant + "/discovery/v2.0/keys";
            return getCertificate(keyUrl, jwtHeader.requiredString("kid"));
        } else if (iss().startsWith("https:")) {
            // TODO: Use authority
            return getCertificate(iss() + "/discovery/keys", jwtHeader.requiredString("kid"));
        } else {
            throw new IllegalArgumentException("Unknown issuer " + iss());
        }
    }

    private static synchronized Certificate getCertificate(String keyUrl, String keyId) throws CertificateException {
        if (!certificates.containsKey(keyId)) {
            try {
                logger.debug("Getting JWK from {}", keyUrl);
                JsonObject keyJson = httpGetJsonObject(new URL(keyUrl));
                certificates.put(keyId, decodeCertificate(getKey(keyJson, keyId)));
            } catch (IOException e) {
                throw ExceptionUtil.softenException(e);
            }
        }
        return certificates.get(keyId);
    }

    private static synchronized Certificate decodeCertificate(String key) throws CertificateException {
        if (certificateFactory == null) {
            certificateFactory = CertificateFactory.getInstance("X.509");
        }
        logger.debug("Decoding certificate {}", key);
        return certificateFactory.generateCertificate(asInputStream(key));
    }

    private static ByteArrayInputStream asInputStream(String key) {
        return new ByteArrayInputStream(Base64.getDecoder().decode(key));
    }

    private static JsonObject httpGetJsonObject(URL keyUrl) throws IOException {
        return JsonParser.parseToObject(IOUtil.toString(keyUrl.openConnection()));
    }

    private static byte[] base64Decode(String string) {
        return Base64.getUrlDecoder().decode(string);
    }

    private static String getKey(JsonObject jsonObject, String keyId) {
        return jsonObject.requiredArray("keys")
                .objectStream()
                .filter(o -> o.requiredString("kid").equals(keyId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't find " + keyId + " in " + jsonObject))
                .requiredArray("x5c").requiredString(0);
    }

    public JsonObject getPayload() {
        return payload;
    }
}
