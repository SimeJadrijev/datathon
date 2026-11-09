package com.datathon.api.ingest.source;

import com.datathon.api.ingest.http.HttpFetchException;
import java.util.Optional;
import java.util.function.IntFunction;
import tools.jackson.databind.JsonNode;

final class WordPressPaging {

    static final int PAGE_SIZE = 100;

    private static final int PAGE_OUT_OF_RANGE = 400;

    private WordPressPaging() {
    }

    static Optional<JsonNode> readPage(int page, IntFunction<JsonNode> reader) {
        try {
            return Optional.of(reader.apply(page));
        } catch (HttpFetchException e) {
            if (page > 1 && e.statusCode() == PAGE_OUT_OF_RANGE) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
