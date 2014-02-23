package com.codahale.metrics;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

public class InstrumentedThreadFactoryTest {
    private final ThreadFactory factory = Executors.defaultThreadFactory();
    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedThreadFactory instrumentedFactory = new InstrumentedThreadFactory(factory, registry, "factory");
    private final ExecutorService executor = Executors.newFixedThreadPool(10, instrumentedFactory);

    @Test
    public void reportsThreadInformation() throws Exception {
        Runnable fastOne = new FastRunnable();
        Meter created = registry.meter("factory.created");
        Meter terminated = registry.meter("factory.terminated");

        assertThat(created.getCount()).isEqualTo(0);
        assertThat(terminated.getCount()).isEqualTo(0);

        // generate demand so the executor service creates the threads through our factory.
        for (int i = 10; i < 20; i++) {
            executor.submit(fastOne);
        }
        assertThat(created.getCount()).isEqualTo(10);
        assertThat(terminated.getCount()).isEqualTo(0);

        // terminate all threads in the executor service.
        executor.shutdown();
        assertThat(executor.awaitTermination(2, TimeUnit.SECONDS)).isTrue();

        // assert that all threads from the factory have been terminated.
        assertThat(terminated.getCount()).isEqualTo(10);
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
                Thread.sleep(500);
            } catch (Exception e) { }
        }
    }

}
