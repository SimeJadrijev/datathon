package com.datathon.api.source;

public record SourceEntry(
        SourceCode code,
        String name,
        String description,
        String url,
        SourceMethod method,
        String license) {

    SourceInfo toInfo() {
        return new SourceInfo(code, name, url);
    }
}
