package com.datathon.api.ingest.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RobotsRulesTest {

    private static final String AGENT = PoliteHttpClient.USER_AGENT;

    @Test
    void appliesRulesForAllAgents() {
        RobotsRules rules = RobotsRules.parse("""
                User-agent: *
                Disallow: /download
                Disallow: /prijavitelj
                """, AGENT);

        assertThat(rules.isAllowed("/download/poziv.pdf")).isFalse();
        assertThat(rules.isAllowed("/prijavitelj")).isFalse();
        assertThat(rules.isAllowed("/pozivi/123")).isTrue();
    }

    @Test
    void prefersGroupForOurAgent() {
        RobotsRules rules = RobotsRules.parse("""
                User-agent: *
                Disallow: /

                User-agent: DatathonBot
                Disallow: /admin
                """, AGENT);

        assertThat(rules.isAllowed("/natjecaji")).isTrue();
        assertThat(rules.isAllowed("/admin/users")).isFalse();
    }

    @Test
    void matchesEncodedAndPlainPaths() {
        RobotsRules rules = RobotsRules.parse("""
                User-agent: *
                Disallow: /docs/12/Javni%20poziv.pdf # comment
                """, AGENT);

        assertThat(rules.isAllowed("/docs/12/Javni poziv.pdf")).isFalse();
        assertThat(rules.isAllowed("/docs/12/Javni%20poziv.pdf")).isFalse();
        assertThat(rules.isAllowed("/docs/13/Javni%20poziv.pdf")).isTrue();
    }

    @Test
    void emptyDisallowAllowsEverything() {
        RobotsRules rules = RobotsRules.parse("User-agent: *\nDisallow:\n", AGENT);

        assertThat(rules.isAllowed("/anything")).isTrue();
    }

    @Test
    void ignoresByteOrderMarkAtTheStart() {
        RobotsRules rules = RobotsRules.parse("\uFEFFUser-agent: *\nDisallow: /docs/kontakti.pdf\n", AGENT);

        assertThat(rules.isAllowed("/docs/kontakti.pdf")).isFalse();
    }
}
