package com.datathon.api.integration.sudreg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.datathon.api.common.ExternalSourceException;
import com.datathon.api.common.NotFoundException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

class RestSudregClientTest {

    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-secret";
    private static final String OIB = "21173725829";

    private HttpServer server;
    private final AtomicInteger tokenRequests = new AtomicInteger();
    private final List<String> detailRequests = new CopyOnWriteArrayList<>();
    private final List<String> authorizations = new CopyOnWriteArrayList<>();
    private volatile String tokenBody = "{\"access_token\":\"token-1\",\"expires_in\":3600}";
    private volatile int detailsStatus = 200;
    private volatile String detailsBody;
    private volatile String tokenBasicAuth;
    private MutableClock clock;

    @BeforeEach
    void startServer() throws IOException {
        detailsBody = fixture("21173725829.json");
        clock = new MutableClock(Instant.parse("2026-10-29T10:00:00Z"));
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/oauth/token", this::handleToken);
        server.createContext("/api/javni/detalji_subjekta", this::handleDetails);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void asksForATokenWithClientCredentialsAndReadsTheCompany() {
        CompanyInfo company = client().findByOib(OIB);

        assertThat(company.name()).startsWith("Microblink");
        assertThat(company.activityCode()).isEqualTo("62.01");
        assertThat(tokenBasicAuth).isEqualTo("Basic " + Base64.getEncoder()
                .encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8)));
        assertThat(authorizations).containsExactly("Bearer token-1");
        assertThat(detailRequests.getFirst()).contains("tip_identifikatora=oib")
                .contains("identifikator=" + OIB).contains("expand_relations=true");
    }

    @Test
    void tokenIsReusedUntilItExpires() {
        RestSudregClient client = client();

        client.findByOib(OIB);
        client.findByOib(OIB);
        assertThat(tokenRequests).hasValue(1);

        clock.advance(Duration.ofHours(2));
        tokenBody = "{\"access_token\":\"token-2\",\"expires_in\":3600}";
        client.findByOib(OIB);

        assertThat(tokenRequests).hasValue(2);
        assertThat(authorizations).containsExactly("Bearer token-1", "Bearer token-1", "Bearer token-2");
    }

    @Test
    void unknownCompanyIsNotFound() {
        detailsStatus = 404;

        assertThatThrownBy(() -> client().findByOib(OIB)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void emptyAnswerIsNotFound() {
        detailsBody = "{}";

        assertThatThrownBy(() -> client().findByOib(OIB)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectedTokenRequestIsAnExternalSourceFailure() {
        server.removeContext("/api/oauth/token");
        server.createContext("/api/oauth/token", exchange -> respond(exchange, 401, "{\"error\":\"invalid_client\"}"));

        assertThatThrownBy(() -> client().findByOib(OIB)).isInstanceOf(ExternalSourceException.class)
                .hasMessageContaining("token request answered HTTP 401");
    }

    @Test
    void serverErrorIsAnExternalSourceFailure() {
        detailsStatus = 500;

        assertThatThrownBy(() -> client().findByOib(OIB)).isInstanceOf(ExternalSourceException.class)
                .hasMessageContaining("HTTP 500");
    }

    @Test
    void unauthorizedAnswerDropsTheTokenSoTheNextCallAsksForANewOne() {
        RestSudregClient client = client();
        client.findByOib(OIB);
        detailsStatus = 401;

        assertThatThrownBy(() -> client.findByOib(OIB)).isInstanceOf(ExternalSourceException.class);
        detailsStatus = 200;
        client.findByOib(OIB);

        assertThat(tokenRequests).hasValue(2);
    }

    @Test
    void unreadableAnswerIsAnExternalSourceFailure() {
        detailsBody = "<html>not json</html>";

        assertThatThrownBy(() -> client().findByOib(OIB)).isInstanceOf(ExternalSourceException.class);
    }

    @Test
    void unreachableRegisterIsAnExternalSourceFailure() {
        server.stop(0);

        assertThatThrownBy(() -> client().findByOib(OIB)).isInstanceOf(ExternalSourceException.class);
    }

    @Test
    void failureMessagesNeverContainTheOibTheKeysOrTheUrl() {
        detailsStatus = 500;
        String secretOib = OIB;

        assertThatThrownBy(() -> client().findByOib(secretOib)).satisfies(failure -> assertThat(failure.getMessage())
                .doesNotContain(secretOib).doesNotContain(CLIENT_ID).doesNotContain(CLIENT_SECRET)
                .doesNotContain("127.0.0.1"));
    }

    @Test
    void propertiesNeverPrintTheCredentials() {
        SudregProperties properties = new SudregProperties("https://example.hr", CLIENT_ID, CLIENT_SECRET);

        assertThat(properties.toString()).doesNotContain(CLIENT_ID).doesNotContain(CLIENT_SECRET);
        assertThat(properties.hasCredentials()).isTrue();
        assertThat(new SudregProperties("https://example.hr", "", "x").hasCredentials()).isFalse();
        assertThat(new SudregProperties("https://example.hr", null, null).hasCredentials()).isFalse();
    }

    private RestSudregClient client() {
        SudregProperties properties = new SudregProperties(
                "http://127.0.0.1:" + server.getAddress().getPort(), CLIENT_ID, CLIENT_SECRET);
        return new RestSudregClient(properties, JsonMapper.builder().build(), clock);
    }

    private void handleToken(HttpExchange exchange) throws IOException {
        tokenRequests.incrementAndGet();
        tokenBasicAuth = exchange.getRequestHeaders().getFirst("Authorization");
        respond(exchange, 200, tokenBody);
    }

    private void handleDetails(HttpExchange exchange) throws IOException {
        detailRequests.add(exchange.getRequestURI().getRawQuery());
        authorizations.add(exchange.getRequestHeaders().getFirst("Authorization"));
        respond(exchange, detailsStatus, detailsStatus == 200 ? detailsBody : "{}");
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String fixture(String name) throws IOException {
        try (InputStream input = new ClassPathResource("fixtures/sudreg/" + name).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
