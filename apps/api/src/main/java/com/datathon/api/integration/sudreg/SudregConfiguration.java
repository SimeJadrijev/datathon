package com.datathon.api.integration.sudreg;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SudregConfiguration {

    @Bean
    @ConditionalOnMissingBean(SudregClient.class)
    SudregClient fixtureSudregClient(JsonMapper jsonMapper) {
        return new FixtureSudregClient(jsonMapper);
    }
}
