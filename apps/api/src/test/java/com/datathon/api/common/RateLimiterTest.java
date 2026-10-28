package com.datathon.api.common;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private static final Duration MINUTE = Duration.ofMinutes(1);

    private static class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-26T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Test
    void eleventhRequestInAMinuteIsRefusedAndPassesAgainAfterTheWindow() {
        MutableClock clock = new MutableClock();
        RateLimiter limiter = new RateLimiter(clock);
        for (int i = 0; i < 10; i++) {
            limiter.check("ip-1", 10, MINUTE);
        }

        assertThatThrownBy(() -> limiter.check("ip-1", 10, MINUTE)).isInstanceOf(TooManyRequestsException.class);
        assertThatCode(() -> limiter.check("ip-2", 10, MINUTE)).doesNotThrowAnyException();

        clock.advance(Duration.ofSeconds(61));
        assertThatCode(() -> limiter.check("ip-1", 10, MINUTE)).doesNotThrowAnyException();
    }
}
