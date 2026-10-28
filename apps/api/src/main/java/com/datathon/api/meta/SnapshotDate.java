package com.datathon.api.meta;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SnapshotDate {

    static final String SEED_FILE = "seed/funding-calls.json";
    private static final String FIELD = "snapshotDate";

    private final LocalDate value;

    public SnapshotDate(JsonMapper jsonMapper) {
        this.value = readFrom(SEED_FILE, jsonMapper);
    }

    public Optional<LocalDate> value() {
        return Optional.ofNullable(value);
    }

    private static LocalDate readFrom(String path, JsonMapper jsonMapper) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return null;
        }
        try (InputStream input = resource.getInputStream(); JsonParser parser = jsonMapper.createParser(input)) {
            return findSnapshotDate(parser);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }

    private static LocalDate findSnapshotDate(JsonParser parser) {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            return null;
        }
        while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
            String name = parser.currentName();
            parser.nextToken();
            if (FIELD.equals(name)) {
                return LocalDate.parse(parser.getString());
            }
            parser.skipChildren();
        }
        return null;
    }
}
