package com.datathon.api.association;

import com.datathon.api.ingest.extract.KeywordClassifier;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

public final class AssociationRegistryParser {

    static final String ACTIVE = "AKTIVAN";

    private static final Pattern OIB = Pattern.compile("\\d{11}");
    private static final int MAX_NAME_CHARS = 1000;
    private static final int MAX_SEAT_CHARS = 500;

    private AssociationRegistryParser() {
    }

    public static int parseActive(InputStream json, JsonMapper jsonMapper, Consumer<AssociationRecord> sink) {
        int count = 0;
        ObjectReader rowReader = jsonMapper.readerFor(JsonNode.class).without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        try (JsonParser parser = jsonMapper.createParser(json)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("The association register is not a JSON array");
            }
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                JsonNode row = rowReader.readValue(parser);
                if (isActiveWithOib(row)) {
                    sink.accept(toRecord(row));
                    count++;
                }
            }
        }
        return count;
    }

    static boolean isActiveWithOib(JsonNode row) {
        return ACTIVE.equals(text(row, "STATUS")) && OIB.matcher(text(row, "OIB")).matches();
    }

    static AssociationRecord toRecord(JsonNode row) {
        String name = cut(text(row, "NAZIV"), MAX_NAME_CHARS);
        String goals = String.join(" ", name, text(row, "CILJEVI"), text(row, "CILJANE_SKUPINE"));
        return new AssociationRecord(
                text(row, "OIB"),
                name,
                cut(text(row, "SJEDISTE"), MAX_SEAT_CHARS),
                text(row, "ZUPANIJA"),
                text(row, "REGISTARSKI_BROJ"),
                KeywordClassifier.areas(goals, text(row, "OPIS_DJELATNOSTI")));
    }

    private static String text(JsonNode row, String field) {
        JsonNode value = row.path(field);
        return value.isString() ? value.asString().strip() : value.isNumber() ? value.asString() : "";
    }

    private static String cut(String value, int maxChars) {
        return value.length() > maxChars ? value.substring(0, maxChars) : value;
    }
}
