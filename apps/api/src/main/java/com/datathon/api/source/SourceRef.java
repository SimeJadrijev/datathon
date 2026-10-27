package com.datathon.api.source;

public record SourceRef(SourceCode code, String name) {

    public static SourceRef from(SourceInfo source) {
        return new SourceRef(source.code(), source.name());
    }
}
