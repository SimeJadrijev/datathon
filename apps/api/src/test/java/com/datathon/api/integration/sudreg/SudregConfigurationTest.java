package com.datathon.api.integration.sudreg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

class SudregConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(JsonMapper.class, () -> JsonMapper.builder().build())
            .withUserConfiguration(SudregConfiguration.class)
            .withPropertyValues("sudreg.base-url=https://sudreg-data.gov.hr");

    @Test
    void withoutKeysTheAppStartsWithTheFixtureClient() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(SudregClient.class)).isInstanceOf(FixtureSudregClient.class);
        });
    }

    @Test
    void withOnlyOneKeyTheFixtureClientIsStillUsed() {
        runner.withPropertyValues("sudreg.client-id=only-id").run(context ->
                assertThat(context.getBean(SudregClient.class)).isInstanceOf(FixtureSudregClient.class));
    }

    @Test
    void withBothKeysTheLiveClientIsUsed() {
        runner.withPropertyValues("sudreg.client-id=id", "sudreg.client-secret=secret").run(context ->
                assertThat(context.getBean(SudregClient.class)).isInstanceOf(RestSudregClient.class));
    }
}
