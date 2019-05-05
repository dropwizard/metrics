package com.codahale.metrics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedExecutorServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentedExecutorServiceTest.class);
    private ExecutorService executor;
    private MetricRegistry registry;
    private InstrumentedExecutorService instrumentedExecutorService;
    private Meter submitted;
    private Counter running;
    private Meter completed;
    private Timer duration;
    private Timer idle;

    @Before
    public void setup() {
        executor = Executors.newCachedThreadPool();
        registry = new MetricRegistry();
        instrumentedExecutorService = new InstrumentedExecutorService(executor, registry, "xs");
        submitted = registry.meter("xs.submitted");
        running = registry.counter("xs.running");
        completed = registry.meter("xs.completed");
        duration = registry.timer("xs.duration");
        idle = registry.timer("xs.idle");
    }


    @Test
    public void reportsTasksInformationForRunnable() throws Exception {

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);
        assertThat(idle.getCount()).isEqualTo(0);

        Runnable runnable = () -> {
            assertThat(submitted.getCount()).isEqualTo(1);
            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isEqualTo(0);
            assertThat(duration.getCount()).isEqualTo(0);
            assertThat(idle.getCount()).isEqualTo(1);
        };

        Future<?> theFuture = instrumentedExecutorService.submit(runnable);

        theFuture.get();

        assertThat(submitted.getCount()).isEqualTo(1);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);
        assertThat(idle.getCount()).isEqualTo(1);
        assertThat(idle.getSnapshot().size()).isEqualTo(1);
    }

    @Test
    public void reportsTasksInformationForCallable() throws Exception {

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);
        assertThat(idle.getCount()).isEqualTo(0);

        Callable<Void> callable = () -> {
            assertThat(submitted.getCount()).isEqualTo(1);
            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isEqualTo(0);
            assertThat(duration.getCount()).isEqualTo(0);
            assertThat(idle.getCount()).isEqualTo(1);
            return null;
        };

        Future<?> theFuture = instrumentedExecutorService.submit(callable);

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
