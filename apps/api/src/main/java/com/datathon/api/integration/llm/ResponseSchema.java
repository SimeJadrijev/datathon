package com.datathon.api.integration.llm;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * Shape of the JSON the model must return: an object with named fields. Each property is a JSON Schema fragment,
 * for example {@code Map.of("type", "string")}. Plain Java types, so no Jackson type reaches the SDK.
 */
@Getter
@Builder
public class ResponseSchema {

    @Singular
    private final Map<String, Map<String, Object>> properties;

    @Singular("requiredField")
    private final List<String> required;
}
