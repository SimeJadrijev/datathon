package com.datathon.api.integration.sudreg;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SudregConfiguration {

    @Bean
    @ConditionalOnProperty(name = "sudreg.fixtures.enabled", havingValue = "true", matchIfMissing = true)
    SudregClient fixtureSudregClient(JsonMapper jsonMapper) {
        return new FixtureSudregClient(jsonMapper);
    }
}
