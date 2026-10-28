package com.datathon.api.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SourceCatalog {

    private static final String RESOURCE = "sources.json";

    private final Map<SourceCode, SourceEntry> entries = new EnumMap<>(SourceCode.class);
    private final Map<SourceCode, SourceInfo> infos = new EnumMap<>(SourceCode.class);

    public SourceCatalog(JsonMapper jsonMapper) {
        for (SourceEntry entry : read(jsonMapper)) {
            if (entries.put(entry.code(), entry) != null) {
                throw new IllegalStateException(RESOURCE + " lists " + entry.code() + " twice");
            }
            infos.put(entry.code(), entry.toInfo());
        }
        Arrays.stream(SourceCode.values())
                .filter(code -> !entries.containsKey(code))
                .findFirst()
                .ifPresent(code -> {
                    throw new IllegalStateException(RESOURCE + " has no entry for " + code);
                });
    }

    public SourceInfo get(SourceCode code) {
        return infos.get(code);
    }

    public List<SourceEntry> entries() {
        return List.copyOf(entries.values());
    }

    private static SourceEntry[] read(JsonMapper jsonMapper) {
        try (InputStream input = new ClassPathResource(RESOURCE).getInputStream()) {
            return jsonMapper.readValue(input, SourceEntry[].class);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }
}
