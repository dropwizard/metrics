package com.codahale.metrics;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

public class InstrumentedExecutorServiceTest {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedExecutorService instrumentedExecutorService = new InstrumentedExecutorService(executor, registry, "xs");

    @Test
    public void reportsTasksInformation() throws Exception {
        Runnable fastOne = new FastRunnable();
        Runnable slowOne = new SlowRunnable();
        Meter submitted = registry.meter("xs.submitted");
        Counter running = registry.counter("xs.running");
        Meter completed = registry.meter("xs.completed");
        Timer duration = registry.timer("xs.duration");

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);

        Future<?> fastFuture = instrumentedExecutorService.submit(fastOne);
        Future<?> slowFuture = instrumentedExecutorService.submit(slowOne);

        assertThat(submitted.getCount()).isEqualTo(2);

        fastFuture.get();
        assertThat(running.getCount()).isEqualTo(1);

        slowFuture.get();
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(duration.getSnapshot().size()).isEqualTo(2);
    }

    @After
    public void tearDown() throws Exception {
        instrumentedExecutorService.shutdown();
        if (!instrumentedExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
            System.err.println("InstrumentedExecutorService did not terminate.");
        }
    }

    private static class FastRunnable implements Runnable {
        @Override
        public void run() {
            // do nothing, die young and leave a good looking corpse.
        }
    }

    private static class SlowRunnable implements Runnable {
        @Override
        public void run() {
            try {
                // sleep a little, then die.
                Thread.sleep(750);
            } catch (Exception e) { }
        }
    }

}
