package com.codahale.metrics;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedExecutorServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentedExecutorServiceTest.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedExecutorService instrumentedExecutorService = new InstrumentedExecutorService(executor, registry, "xs");

    @Test
    public void reportsTasksInformation() throws Exception {
        final Meter submitted = registry.meter("xs.submitted");
        final Counter running = registry.counter("xs.running");
        final Meter completed = registry.meter("xs.completed");
        final Timer duration = registry.timer("xs.duration");
        final Timer idle = registry.timer("xs.idle");

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);
        assertThat(idle.getCount()).isEqualTo(0);

        Future<?> theFuture = instrumentedExecutorService.submit(() -> {
            assertThat(submitted.getCount()).isEqualTo(1);
            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isEqualTo(0);
            assertThat(duration.getCount()).isEqualTo(0);
            assertThat(idle.getCount()).isEqualTo(1);
    });

        theFuture.get();

        assertThat(submitted.getCount()).isEqualTo(1);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);
        assertThat(idle.getCount()).isEqualTo(1);
        assertThat(idle.getSnapshot().size()).isEqualTo(1);
    }

    @After
    public void tearDown() throws Exception {
        instrumentedExecutorService.shutdown();
        if (!instrumentedExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
            LOGGER.error("InstrumentedExecutorService did not terminate.");
        }
    }

}
