package com.datathon.api.integration.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.errors.AnthropicInvalidDataException;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The SDK handles auth, retries (429, 5xx) and timeouts. The answer is the JSON text of the first text block,
 * parsed with our own (Jackson 3) mapper, so no SDK type leaves this class.
 */
@Slf4j
@RequiredArgsConstructor
public class AnthropicLlmClient implements LlmClient {

    private final LlmProperties properties;
    private final MessageParamsFactory paramsFactory;
    private final LlmUsageService usageService;
    private final JsonMapper jsonMapper;
    private final AnthropicClient client;

    @Override
    public JsonNode complete(LlmRequest request) {
        usageService.assertBudgetAvailable();
        String model = request.model() != null ? request.model() : properties.getModel();
        Message message = send(paramsFactory.create(request, model));
        try {
            usageService.record(model, request.purpose(), (int) message.usage().inputTokens(),
                    (int) message.usage().outputTokens());
        } catch (AnthropicInvalidDataException e) {
            throw new LlmResponseException("Claude API response has no usage data", e);
        }
        assertComplete(message);
        return parseJson(message);
    }

    private Message send(MessageCreateParams params) {
        try {
            return client.messages().create(params);
        } catch (AnthropicInvalidDataException e) {
            throw new LlmResponseException("Claude API response is invalid", e);
        } catch (AnthropicException e) {
            throw new LlmUnavailableException("Claude API is unavailable: " + e.getMessage(), e);
        }
    }

    /** A cut-off or refused answer must not be saved as "the model found nothing". */
    private void assertComplete(Message message) {
        StopReason.Known stopReason = message.stopReason().map(StopReason::known).orElse(null);
        if (stopReason == StopReason.Known.MAX_TOKENS || stopReason == StopReason.Known.REFUSAL) {
            throw new LlmResponseException("Model answer is not usable, stop reason: " + stopReason);
        }
    }

    private JsonNode parseJson(Message message) {
        for (ContentBlock block : message.content()) {
            if (block.isText()) {
                return readJson(block.asText().text());
            }
        }
        throw new LlmResponseException("Model did not return a structured result");
    }

    private JsonNode readJson(String text) {
        try {
            JsonNode node = jsonMapper.readTree(text);
            if (!node.isObject()) {
                throw new LlmResponseException("Model answer is not a JSON object");
            }
            return node;
        } catch (JacksonException e) {
            throw new LlmResponseException("Model answer is not valid JSON", e);
        }
    }
}
