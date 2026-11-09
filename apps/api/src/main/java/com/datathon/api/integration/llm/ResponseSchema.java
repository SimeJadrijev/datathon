package com.datathon.api.integration.llm;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class ResponseSchema {

    @Singular
    private final Map<String, Map<String, Object>> properties;

    @Singular("requiredField")
    private final List<String> required;
}
