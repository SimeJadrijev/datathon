package com.datathon.api.integration.llm;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds the SDK request. The answer is forced into the requested {@link ResponseSchema} with structured outputs
 * (JSON schema), the same way as in the POC, so the model cannot return a value outside an enum.
 */
@Component
@RequiredArgsConstructor
public class MessageParamsFactory {

    private final LlmProperties properties;

    public MessageCreateParams create(LlmRequest request, String model) {
        return MessageCreateParams.builder()
                .model(model)
                .maxTokens(properties.getMaxTokens())
                .system(request.systemPrompt())
                .addUserMessage(request.userText())
                .outputConfig(OutputConfig.builder()
                        .effort(OutputConfig.Effort.LOW)
                        .format(JsonOutputFormat.builder().schema(toJsonSchema(request.responseSchema())).build())
                        .build())
                .build();
    }

    private JsonOutputFormat.Schema toJsonSchema(ResponseSchema schema) {
        Map<String, Object> properties = new LinkedHashMap<>(schema.getProperties());
        return JsonOutputFormat.Schema.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(properties))
                .putAdditionalProperty("required", JsonValue.from(schema.getRequired()))
                .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                .build();
    }
}
