package com.datathon.api.integration.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.errors.AnthropicInvalidDataException;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.ToolUseBlock;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The SDK handles auth, retries (429, 5xx) and timeouts; it uses its own Jackson 2 internally, so the answer
 * crosses the boundary as a plain Map and never as a Jackson type.
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
        return extractToolInput(message);
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

    @SuppressWarnings("unchecked")
    private JsonNode extractToolInput(Message message) {
        for (ContentBlock block : message.content()) {
            if (!block.isToolUse()) {
                continue;
            }
            ToolUseBlock toolUse = block.asToolUse();
            Map<String, Object> input = toolUse._input().convert(Map.class);
            if (input != null) {
                return jsonMapper.valueToTree(input);
            }
        }
        throw new LlmResponseException("Model did not return a structured result");
    }
}
