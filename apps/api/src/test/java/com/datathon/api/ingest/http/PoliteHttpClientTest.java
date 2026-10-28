package com.datathon.api.ingest.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class PoliteHttpClientTest {

    private static final Duration FAST = Duration.ofMillis(10);

    @TempDir
    private Path cacheDir;

    private HttpServer server;
    private final Map<String, AtomicInteger> hits = new ConcurrentHashMap<>();
    private final AtomicInteger flakyFailuresLeft = new AtomicInteger(2);

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void doesNotFetchPathDisallowedInRobotsTxt() {
        PoliteHttpClient client = client(FAST);

        assertThatThrownBy(() -> client.getText(url("/private/report.pdf")))
                .isInstanceOf(RobotsBlockedException.class);
        assertThat(hitsOf("/private/report.pdf")).isZero();
        assertThat(client.getText(url("/public/page"))).isEqualTo("ok /public/page");
    }

    @Test
    void sendsAtMostOneRequestPerSecondToTheSameHost() {
        PoliteHttpClient client = client(Duration.ofSeconds(1));
        client.getText(url("/warm-up"));

        long start = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            client.getText(url("/page/" + i));
        }

        assertThat(Duration.ofNanos(System.nanoTime() - start)).isGreaterThanOrEqualTo(Duration.ofSeconds(4));
    }

    @Test
    void servesSecondCallFromCache() {
        PoliteHttpClient client = client(FAST);

        String first = client.getText(url("/cached"));
        String second = client.getText(url("/cached"));

        assertThat(second).isEqualTo(first);
        assertThat(hitsOf("/cached")).isEqualTo(1);
    }

    @Test
    void refreshSkipsTheCache() {
        PoliteHttpClient client = client(FAST);

        client.getText(url("/cached"));
        client.getText(url("/cached"), true);

        assertThat(hitsOf("/cached")).isEqualTo(2);
    }

    @Test
    void decodesWindows1250Response() {
        assertThat(client(FAST).getText(url("/cp1250"))).isEqualTo("Natječaj za udruge: čćžšđ");
    }

    @Test
    void retriesTemporaryServerErrors() {
        assertThat(client(FAST).getText(url("/flaky"))).isEqualTo("ok /flaky");
        assertThat(hitsOf("/flaky")).isEqualTo(3);
    }

    @Test
    void failsOnNotFoundWithoutRetrying() {
        assertThatThrownBy(() -> client(FAST).getText(url("/missing")))
                .isInstanceOf(HttpFetchException.class)
                .hasMessageContaining("404");
        assertThat(hitsOf("/missing")).isEqualTo(1);
    }

    private PoliteHttpClient client(Duration minInterval) {
        return new PoliteHttpClient(new DiskCache(cacheDir), JsonMapper.builder().build(), minInterval, FAST);
    }

    private String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    private int hitsOf(String path) {
        return hits.getOrDefault(path, new AtomicInteger()).get();
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        hits.computeIfAbsent(path, p -> new AtomicInteger()).incrementAndGet();
        switch (path) {
            case "/robots.txt" -> respond(exchange, 200,
                    "User-agent: *\nDisallow: /private\n".getBytes(StandardCharsets.UTF_8));
            case "/cp1250" -> respond(exchange, 200,
                    "Natječaj za udruge: čćžšđ".getBytes(Charset.forName("windows-1250")));
            case "/missing" -> respond(exchange, 404, new byte[0]);
            case "/flaky" -> {
                if (flakyFailuresLeft.getAndDecrement() > 0) {
                    respond(exchange, 503, new byte[0]);
                } else {
                    respond(exchange, 200, "ok /flaky".getBytes(StandardCharsets.UTF_8));
                }
            }
            default -> respond(exchange, 200, ("ok " + path).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void respond(HttpExchange exchange, int status, byte[] body) throws IOException {
        exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }
}
