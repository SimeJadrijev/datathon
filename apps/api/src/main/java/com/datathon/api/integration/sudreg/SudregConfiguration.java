package com.datathon.api.integration.sudreg;

import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Configuration
@EnableConfigurationProperties(SudregProperties.class)
public class SudregConfiguration {

    @Bean
    SudregClient sudregClient(SudregProperties properties, JsonMapper jsonMapper) {
        if (properties.hasCredentials()) {
            log.info("Sudreg: using the live register at {}", properties.baseUrl());
            return new RestSudregClient(properties, jsonMapper, Clock.systemUTC());
        }
        log.info("Sudreg: no credentials set, using saved fixtures");
        return new FixtureSudregClient(jsonMapper);
    }
}
