package com.codahale.metrics;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class InstrumentedScheduledExecutorServiceTest {
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @After
    public void tearDown() {
        executorService.shutdown();
    }

    @Test
    public void testCreate() throws Exception {
        MetricRegistry registry = new MetricRegistry();
        InstrumentedScheduledExecutorService instrumentedExecutorService = new InstrumentedScheduledExecutorService(
                executorService, registry, "test-scheduled-instrumented");
        CountDownLatch countDownLatch = new CountDownLatch(10);
        instrumentedExecutorService.scheduleAtFixedRate(countDownLatch::countDown, 0, 10, TimeUnit.MILLISECONDS);
        countDownLatch.await(5, TimeUnit.SECONDS);
        instrumentedExecutorService.shutdown();

        assertThat(registry.getMetrics()).containsOnlyKeys("test-scheduled-instrumented.completed",
                "test-scheduled-instrumented.submitted", "test-scheduled-instrumented.duration", "test-scheduled-instrumented.running",
                "test-scheduled-instrumented.scheduled.once", "test-scheduled-instrumented.scheduled.overrun",
                "test-scheduled-instrumented.scheduled.percent-of-period", "test-scheduled-instrumented.scheduled.repetitively");
    }
}
