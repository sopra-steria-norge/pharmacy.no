package no.pharmacy.infrastructure.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

public class JwtTokenTest {

    private String sampleToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6InV1a2hnb2VBbGdLR0NxTzlmWVlWNGZOMWtUZyIsImtpZCI6InV1a2hnb2VBbGdLR0NxTzlmWVlWNGZOMWtUZyJ9.eyJhdWQiOiJhMjg0NmM4MS01OTU5LTQxODctOGJlYy03NmNiNGZhYWUyY2IiLCJpc3MiOiJodHRwczovL2RpZmEtYWRmcy1pZi5kaWZhLmFkL2FkZnMiLCJpYXQiOjE0OTM0MDc4NjUsImV4cCI6MTQ5MzQxMTQ2NSwiYXV0aF90aW1lIjoxNDkzNDA3ODE3LCJub25jZSI6Ijk4M2NiZGJkLWE5ZTktNGEwNC04ZjY3LWE1ZDI0Mzc5MGE0MSIsInN1YiI6IlZOZTZMdjBVK1hhaWlGT1pVdmFpY0puWCtER0lISmZIL0ovVzQzd3Q2ZnM9IiwidXBuIjoiam9oYW5uZXNib290c0BkaWZhLmFkIiwicHdkX2V4cCI6IjI1OTI1NDAiLCJ1bmlxdWVfbmFtZSI6IkpvaGFubmVzIEFwb3Rla25payIsIkhFUi1udW1iZXJzIjoiOTAzOTYiLCJVc2VyR3JvdXBzIjoiZGlmYVxcRG9tYWluIFVzZXJzIiwiSFBSLW51bWJlciI6IjIwMTQ3NzciLCJyb2xlIjoiUm9sZSIsImFwcHR5cGUiOiJQdWJsaWMiLCJhcHBpZCI6ImEyODQ2YzgxLTU5NTktNDE4Ny04YmVjLTc2Y2I0ZmFhZTJjYiIsImF1dGhtZXRob2QiOiJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydCIsInZlciI6IjEuMCIsInNjcCI6Im9wZW5pZCJ9.nN3_PpMEt9JgCxZyCxVx7hDrpALhDUuB3ZjpuKFFAiH0IPWKE3cvxKu_j_5NVRBT6U3kL96wfwa6FxsfoXnXUhMqULqztpErZK7Sy1QD3VWWxljbm1cmkHtudSMBAEsqZx209lcft_wmQHkbG9KBWZ6ZbE-kftCpGNk8ppC7oEYYqJg_rnBNR5ppi6nDqHk8hGyth7xPN5p1AMVv8EFjdfjkSlmLtKknL4iemRRo_uDcuVlESF8b2CLbmFR80LHpGpI9LrYfNd5A-1N8bltcPIT8lSwDhWIvRKP6ULPhdF1aI-dGL_FyNL-ecyknphuh1cXMbtQ4H2qeWH3dGpCX8g";
    private JwtToken jwtToken = new JwtToken(sampleToken);
    private Instant tokenCreationTime = Instant.ofEpochMilli(1493407817000L);

    @Test
    public void shouldReadTokenProperties() throws Exception {
        assertThat(jwtToken.aud()).isEqualTo("a2846c81-5959-4187-8bec-76cb4faae2cb");
    }

    @Test
    public void shouldValidateTokenSignature() throws GeneralSecurityException, IOException {
        assertThat(jwtToken.verifySignature()).isTrue();
    }

    @Test
    public void shouldAcceptNonexpiredToken() throws Exception {
        assertThat(tokenCreationTime.atZone(ZoneId.systemDefault()).getYear()).isEqualTo(2017);
        assertThat(jwtToken.authTime()).hasValue(tokenCreationTime);
        assertThat(jwtToken.claim("auth_time")).hasValue(String.valueOf(tokenCreationTime.toEpochMilli()/1000));
        assertThat(jwtToken.verifyTimeValidity(tokenCreationTime)).isTrue();
    }

    @Test
    public void shouldRejectExpiredToken() throws Exception {
        assertThat(jwtToken.verifyTimeValidity(tokenCreationTime.plusSeconds(60*60*24))).isFalse();
    }

    @Test
    public void shouldRejectFutureToken() throws Exception {
        assertThat(jwtToken.verifyTimeValidity(
                tokenCreationTime.minusSeconds(60*60*24))).isFalse();
    }

    @Test
    public void shouldRejectForgedToken() throws Exception {
        String[] tokenValues = sampleToken.split("\\.");

        JsonObject claims = JsonParser.parseToObject(new String(Base64.getUrlDecoder().decode(tokenValues[1])));
        claims.put("username", UUID.randomUUID().toString());
        tokenValues[1] = Base64.getUrlEncoder().encodeToString(claims.toString().getBytes());

        assertThat(new JwtToken(tokenValues[0] + "." + tokenValues[1] + "." + tokenValues[2]).verifySignature())
            .isFalse();
    }
}
