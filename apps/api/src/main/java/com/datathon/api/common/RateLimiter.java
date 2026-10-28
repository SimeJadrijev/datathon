package com.datathon.api.common;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** In-memory sliding window per key. Enough for one instance (the demo); not shared between instances. */
@Component
public class RateLimiter {

    private static final int PRUNE_THRESHOLD = 10_000;

    private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiter() {
        this(Clock.systemUTC());
    }

    RateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Throws {@link TooManyRequestsException} when {@code key} already made {@code limit} requests in the window. */
    public void check(String key, int limit, Duration window) {
        Instant now = clock.instant();
        if (requests.size() > PRUNE_THRESHOLD) {
            prune(now.minus(window));
        }
        Deque<Instant> timestamps = requests.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(now.minus(window))) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= limit) {
                throw new TooManyRequestsException("More than " + limit + " requests in " + window.toSeconds() + " s");
            }
            timestamps.addLast(now);
        }
    }

    private void prune(Instant cutoff) {
        requests.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                Instant last = entry.getValue().peekLast();
                return last == null || last.isBefore(cutoff);
            }
        });
    }
}
