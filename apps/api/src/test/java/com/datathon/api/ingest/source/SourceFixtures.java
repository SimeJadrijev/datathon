package com.datathon.api.ingest.source;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

final class SourceFixtures {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private SourceFixtures() {
    }

    static String text(String path) {
        try (InputStream input = new ClassPathResource("fixtures/" + path).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Missing fixture " + path, e);
        }
    }

    static JsonNode json(String path) {
        return JSON.readTree(text(path));
    }
}
