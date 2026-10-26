package com.datathon.api.ai;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Runs an admin batch in the background. One batch per name at a time, so a double click cannot burn the budget. */
@Component
@Slf4j
public class BatchRunner {

    private final Set<String> running = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /** Throws {@link IllegalStateException} when a batch with this name is already running. */
    public void start(String name, Runnable job) {
        if (!running.add(name)) {
            throw new IllegalStateException("Batch '" + name + "' is already running");
        }
        executor.submit(() -> {
            try {
                job.run();
            } catch (RuntimeException e) {
                log.error("Batch '{}' failed", name, e);
            } finally {
                running.remove(name);
            }
        });
    }
}
