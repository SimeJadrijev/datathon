package com.datathon.api.integration.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datathon.api.ai.LlmUsage;
import com.datathon.api.ai.LlmUsageRepository;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class AnthropicLlmClientTest {

    private static final String JSON_ANSWER = "{\"deadline\":\"2026-12-01\",\"amount\":null}";
    private static final String ANSWER_RESPONSE = response("end_turn", JSON_ANSWER);

    private static String response(String stopReason, String text) {
        String escaped = text.replace("\"", "\\\"");
        return "{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"model\":\"claude-haiku-5-5\","
                + "\"content\":[{\"type\":\"text\",\"text\":\"" + escaped + "\"}],\"stop_reason\":\"" + stopReason + "\","
                + "\"usage\":{\"input_tokens\":1000,\"output_tokens\":200}}";
    }

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final Deque<FakeResponse> responses = new ArrayDeque<>();
    private final AtomicInteger requestCount = new AtomicInteger();
    private HttpServer server;
    private LlmUsageRepository usageRepository;
    private AnthropicLlmClient client;

    record FakeResponse(int status, String body) {}

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/v1/messages", exchange -> {
            requestCount.incrementAndGet();
            exchange.getRequestBody().readAllBytes();
            FakeResponse response = responses.poll();
            byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(response.status(), bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();

        usageRepository = mock(LlmUsageRepository.class);
        when(usageRepository.sumCostUsd()).thenReturn(BigDecimal.ZERO);
        LlmProperties.Price price = new LlmProperties.Price(new BigDecimal("0.10"), new BigDecimal("0.50"));
        LlmProperties properties = new LlmProperties("test-key", "http://localhost:" + server.getAddress().getPort(),
                "claude-haiku-5-5", new BigDecimal("2.0"), 1024, 2, 5, 3, Map.of("claude-haiku-5-5", price));
        client = new AnthropicLlmClient(properties, new MessageParamsFactory(properties),
                new LlmUsageService(usageRepository, properties), jsonMapper,
                new AnthropicClientFactory().create(properties));
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private LlmRequest request() {
        return new LlmRequest("test", "system", "text", ResponseSchema.builder()
                .property("deadline", Map.of("type", List.of("string", "null")))
                .property("amount", Map.of("type", List.of("number", "null")))
                .requiredField("deadline")
                .build());
    }

    @Test
    void returnsToolInputIncludingNullField() {
        responses.add(new FakeResponse(200, ANSWER_RESPONSE));

        JsonNode result = client.complete(request());

        assertThat(result.get("deadline").asString()).isEqualTo("2026-12-01");
        assertThat(result.get("amount").isNull()).isTrue();
    }

    @Test
    void savesUsageWithCost() {
        responses.add(new FakeResponse(200, ANSWER_RESPONSE));

        client.complete(request());

        ArgumentCaptor<LlmUsage> captor = ArgumentCaptor.forClass(LlmUsage.class);
        verify(usageRepository).save(captor.capture());
        assertThat(captor.getValue().getInputTokens()).isEqualTo(1000);
        assertThat(captor.getValue().getOutputTokens()).isEqualTo(200);
        assertThat(captor.getValue().getCostUsd()).isEqualByComparingTo("0.000200");
    }

    @Test
    void invalidJsonThrowsResponseException() {
        responses.add(new FakeResponse(200, "not json {"));

        assertThatThrownBy(() -> client.complete(request())).isInstanceOf(LlmResponseException.class);
    }

    @Test
    void answerWithoutTextBlockThrowsResponseException() {
        responses.add(new FakeResponse(200, "{\"content\":[],\"stop_reason\":\"end_turn\",\"usage\":{\"input_tokens\":10,\"output_tokens\":5}}"));

        assertThatThrownBy(() -> client.complete(request())).isInstanceOf(LlmResponseException.class);
    }

    @Test
    void textThatIsNotJsonThrowsResponseException() {
        responses.add(new FakeResponse(200, response("end_turn", "sorry, no json")));

        assertThatThrownBy(() -> client.complete(request())).isInstanceOf(LlmResponseException.class);
    }

    @Test
    void cutOffAnswerIsNotUsed() {
        responses.add(new FakeResponse(200, response("max_tokens", JSON_ANSWER)));

        assertThatThrownBy(() -> client.complete(request()))
                .isInstanceOf(LlmResponseException.class)
                .hasMessageContaining("MAX_TOKENS");
    }

    @Test
    void retriesOn429() {
        responses.add(new FakeResponse(429, "{}"));
        responses.add(new FakeResponse(200, ANSWER_RESPONSE));

        JsonNode result = client.complete(request());

        assertThat(result.get("deadline").asString()).isEqualTo("2026-12-01");
        assertThat(requestCount).hasValue(2);
    }

    @Test
    void givesUpAfterMaxAttempts() {
        responses.add(new FakeResponse(503, "{}"));
        responses.add(new FakeResponse(503, "{}"));
        responses.add(new FakeResponse(503, "{}"));

        assertThatThrownBy(() -> client.complete(request())).isInstanceOf(LlmUnavailableException.class);
        assertThat(requestCount).hasValue(3);
    }

    @Test
    void budgetReachedBlocksCallWithoutRequest() {
        when(usageRepository.sumCostUsd()).thenReturn(new BigDecimal("2.0"));

        assertThatThrownBy(() -> client.complete(request()))
                .isInstanceOf(LlmBudgetExceededException.class)
                .hasMessageContaining("budget");
        assertThat(requestCount).hasValue(0);
        verify(usageRepository, never()).save(any());
    }

    @Test
    void noopClientIsUnavailable() {
        assertThatThrownBy(() -> new NoopLlmClient().complete(request()))
                .isInstanceOf(LlmUnavailableException.class);
    }
}
