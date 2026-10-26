package com.datathon.api.integration.llm;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolChoiceTool;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds the SDK request. Structured output is forced with a single tool whose input schema is the requested
 * {@link ResponseSchema}, converted into the SDK's own types.
 */
@Component
@RequiredArgsConstructor
public class MessageParamsFactory {

    static final String TOOL_NAME = "return_result";

    private final LlmProperties properties;

    public MessageCreateParams create(LlmRequest request, String model) {
        return MessageCreateParams.builder()
                .model(model)
                .maxTokens(properties.getMaxTokens())
                .system(request.systemPrompt())
                .addUserMessage(request.userText())
                .addTool(buildTool(request.responseSchema()))
                .toolChoice(ToolChoiceTool.builder().name(TOOL_NAME).build())
                .build();
    }

    private Tool buildTool(ResponseSchema schema) {
        return Tool.builder()
                .name(TOOL_NAME)
                .description("Return the result in the required structure.")
                .inputSchema(Tool.InputSchema.builder()
                        .properties(toProperties(schema.getProperties()))
                        .required(schema.getRequired())
                        .build())
                .build();
    }

    private Tool.InputSchema.Properties toProperties(Map<String, Map<String, Object>> properties) {
        Map<String, JsonValue> converted = new HashMap<>();
        properties.forEach((name, definition) -> converted.put(name, JsonValue.from(definition)));
        return Tool.InputSchema.Properties.builder().additionalProperties(converted).build();
    }
}
