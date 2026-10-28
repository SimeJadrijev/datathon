package com.datathon.api.integration.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.Timeout;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class AnthropicClientFactory {

    public AnthropicClient create(LlmProperties properties) {
        return AnthropicOkHttpClient.builder()
                .apiKey(properties.getApiKey())
                .baseUrl(properties.getBaseUrl())
                .maxRetries(Math.max(0, properties.getMaxAttempts() - 1))
                .timeout(timeout(properties))
                .build();
    }

    private Timeout timeout(LlmProperties properties) {
        Duration read = Duration.ofSeconds(properties.getReadTimeoutSeconds());
        return Timeout.builder()
                .connect(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
                .read(read)
                .request(read)
                .build();
    }
}
