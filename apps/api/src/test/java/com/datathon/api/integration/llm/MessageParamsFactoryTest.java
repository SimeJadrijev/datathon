package com.datathon.api.integration.llm;

import static org.assertj.core.api.Assertions.assertThat;

import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MessageParamsFactoryTest {

    private static final ResponseSchema SCHEMA = ResponseSchema.builder()
            .property("deadline", Map.of("type", List.of("string", "null")))
            .requiredField("deadline")
            .build();

    private final MessageParamsFactory factory = new MessageParamsFactory(new LlmProperties("key", "http://localhost",
            "claude-haiku-5-5", "claude-sonnet-5-5", new BigDecimal("2.0"), 1024, 2, 5, 3, Map.of()));

    @Test
    void scannedPdfGoesBeforeTheTextAsADocumentBlock() {
        LlmRequest request = new LlmRequest("test", null, "system", "text", SCHEMA, List.of(new byte[] {1, 2, 3}));

        MessageCreateParams params = factory.create(request, "claude-haiku-5-5");

        List<ContentBlockParam> content = params.messages().getFirst().content().asContentBlockParams();
        assertThat(content).hasSize(2);
        assertThat(content.get(0).isDocument()).isTrue();
        assertThat(content.get(1).asText().text()).isEqualTo("text");
    }

    @Test
    void requestWithoutDocumentsStaysPlainText() {
        MessageCreateParams params = factory.create(new LlmRequest("test", "system", "text", SCHEMA), "claude-haiku-5-5");

        assertThat(params.messages().getFirst().content().isString()).isTrue();
    }
}
