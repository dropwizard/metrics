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
        final Meter submitted = registry.meter("xs.submitted");
        final Counter running = registry.counter("xs.running");
        final Meter completed = registry.meter("xs.completed");
        final Timer duration = registry.timer("xs.duration");

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);

        Future<?> theFuture = instrumentedExecutorService.submit(new Runnable() {
            public void run() {
                assertThat(submitted.getCount()).isEqualTo(1);
                assertThat(running.getCount()).isEqualTo(1);
                assertThat(completed.getCount()).isEqualTo(0);
                assertThat(duration.getCount()).isEqualTo(0);
	    }
	});

        theFuture.get();

        assertThat(submitted.getCount()).isEqualTo(1);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);
    }

    @After
    public void tearDown() throws Exception {
        instrumentedExecutorService.shutdown();
        if (!instrumentedExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
            System.err.println("InstrumentedExecutorService did not terminate.");
        }
    }

}
