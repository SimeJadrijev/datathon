package com.datathon.api.ai;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BatchRunner {

    private final Set<String> running = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newCachedThreadPool();

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
