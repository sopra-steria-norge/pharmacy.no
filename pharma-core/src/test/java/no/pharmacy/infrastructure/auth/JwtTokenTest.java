package no.pharmacy.infrastructure.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;
import org.junit.Test;

public class JwtTokenTest {

    private String sampleToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzUU4wQlpTN3M0bk4tQmRyamJGMFlfTGRNTSIsImtpZCI6ImEzUU4wQlpTN3M0bk4tQmRyamJGMFlfTGRNTSJ9.eyJhdWQiOiIzZWY0NDdjZS1kZmQ0LTRjYzItYTA2MS1hOTBlNmJkYjY1MTMiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9iYmY3N2E4Zi03ZmE4LTQ1YTAtODFkYi03YWQzNDVhMzQ5YzAvIiwiaWF0IjoxNDkxMzI5NDM2LCJuYmYiOjE0OTEzMjk0MzYsImV4cCI6MTQ5MTMzMzMzNiwiYW1yIjpbInB3ZCJdLCJjX2hhc2giOiItWUlCdmVSeW1yOVc5VC1DbzFLdmtnIiwiZmFtaWx5X25hbWUiOiJCcm9kd2FsbCIsImdpdmVuX25hbWUiOiJKb2hhbm5lcyIsImlwYWRkciI6Ijk1LjM0LjEwNS4yMzkiLCJuYW1lIjoiSm9oYW5uZXMgQnJvZHdhbGwiLCJub25jZSI6Ijg4ODhlOThhLWNiNjYtNGQyOS04ZDM0LTQ1YzBlNGZkYmY2OCIsIm9pZCI6IjRiMjg5MGJmLWI5NmEtNGE4ZC1iZjE2LTU3NjRmMGJkYzEyOSIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS00MDU3NzczNzI0LTQyNDg2MTA3MzQtMzU1MTUxODkxMC0zMzcwMSIsInBsYXRmIjoiMyIsInN1YiI6IjNMV3J3MWZNeFF6cVdDbmlwdW95T2RRUC1hMXQwVFZ6eG9oaVBfX2VtQjAiLCJ0aWQiOiJiYmY3N2E4Zi03ZmE4LTQ1YTAtODFkYi03YWQzNDVhMzQ5YzAiLCJ1bmlxdWVfbmFtZSI6ImpvaGFubmVzLmJyb2R3YWxsQHJlbWEubm8iLCJ1cG4iOiJqb2hhbm5lcy5icm9kd2FsbEByZW1hLm5vIiwidXRpIjoiOEFLTGI0YkYza0NudlozZ2pHOG9BQSIsInZlciI6IjEuMCJ9.YdYahrRcar5x1hYjCFEgdo8td52qJ0Dvwi12vUAWl02mamwD4V1xjV6weXzQe9PBHlAkNSiEfWaKyiZN5ldXlIEdMb29GX4-r6D7U5v-bR2urXRQP0YS7oKR7-5YOJV-LAQ5Z7nWm1Vhp3_7gak7srn3YtBdjH1p-GdDClFGrZYHEyVOOUrYvJncUwmFhbGSa9H8YNjEtRUJN0r-1iw4saioUld9y4oS71uUmTM3R1srlU01GP7X_PJj1oDUX24eg_Iss0EDfE4gY3-AofZsYfNBzUjqQIXjV9R_2ysR0Mua3kCxo0B3ktldlxKPNuzeialpFx3VSDIdwUVuJBULTA";
    private JwtToken jwtToken = new JwtToken(sampleToken);
    private Instant tokenCreationTime = Instant.ofEpochMilli(1491330363304L);

    @Test
    public void shouldReadTokenProperties() throws Exception {
        assertThat(jwtToken.aud()).isEqualTo("3ef447ce-dfd4-4cc2-a061-a90e6bdb6513");
    }

    @Test
    public void shouldValidateTokenSignature() throws GeneralSecurityException, IOException {
        assertThat(jwtToken.verifySignature()).isTrue();
    }

    @Test
    public void shouldAcceptNonexpiredToken() throws Exception {
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
