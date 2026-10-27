package com.datathon.api.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SourceCatalog {

    private static final String RESOURCE = "sources.json";

    private final Map<SourceCode, SourceInfo> sources = new EnumMap<>(SourceCode.class);

    public SourceCatalog(JsonMapper jsonMapper) {
        for (SourceInfo source : read(jsonMapper)) {
            if (sources.put(source.code(), source) != null) {
                throw new IllegalStateException(RESOURCE + " lists " + source.code() + " twice");
            }
        }
        Arrays.stream(SourceCode.values())
                .filter(code -> !sources.containsKey(code))
                .findFirst()
                .ifPresent(code -> {
                    throw new IllegalStateException(RESOURCE + " has no entry for " + code);
                });
    }

    public SourceInfo get(SourceCode code) {
        return sources.get(code);
    }

    private static SourceInfo[] read(JsonMapper jsonMapper) {
        try (InputStream input = new ClassPathResource(RESOURCE).getInputStream()) {
            return jsonMapper.readValue(input, SourceInfo[].class);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }
}
