package com.codahale.metrics;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class InstrumentedExecutorServiceTest {


    @Test
    public void testCreate() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MetricRegistry registry = new MetricRegistry();
        InstrumentedExecutorService instrumentedExecutorService = new InstrumentedExecutorService(executorService,
                registry, "test-instrumented");
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            instrumentedExecutorService.submit(countDownLatch::countDown);
        }
        countDownLatch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(registry.getMetrics()).containsOnlyKeys("test-instrumented.completed",
                "test-instrumented.submitted", "test-instrumented.duration", "test-instrumented.idle",
                "test-instrumented.running");
    }
}
