package com.codahale.metrics;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class CachedGaugeTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedGaugeTest.class);
    private static final int THREAD_COUNT = 10;

    private final AtomicInteger value = new AtomicInteger(0);
    private final Gauge<Integer> gauge = new CachedGauge<Integer>(100, TimeUnit.MILLISECONDS) {
        @Override
        protected Integer loadValue() {
            return value.incrementAndGet();
        }
    };
    private final Gauge<Integer> shortTimeoutGauge = new CachedGauge<Integer>(1, TimeUnit.MILLISECONDS) {
        @Override
        protected Integer loadValue() {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread was interrupted", e);
            }
            return value.incrementAndGet();
        }
    };
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    @Test
    public void cachesTheValueForTheGivenPeriod() {
        assertThat(gauge.getValue())
                .isEqualTo(1);
        assertThat(gauge.getValue())
                .isEqualTo(1);
    }

    @Test
    public void reloadsTheCachedValueAfterTheGivenPeriod() throws Exception {
        assertThat(gauge.getValue())
                .isEqualTo(1);

        Thread.sleep(150);

        assertThat(gauge.getValue())
                .isEqualTo(2);

        assertThat(gauge.getValue())
                .isEqualTo(2);
    }

    @Test
    public void multipleThreadAccessReturnsConsistentResults() throws Exception {
        List<Future<Boolean>> futures = new ArrayList<>(THREAD_COUNT);
        long runningTimeMillis = TimeUnit.SECONDS.toMillis(10);

        for (int i = 0; i < THREAD_COUNT; i++) {
            Future<Boolean> future = executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                int lastValue = 0;

                do {
                    Integer newValue = shortTimeoutGauge.getValue();

                    if (newValue == null) {
                        LOGGER.warn("Cached gauge returned null value");
                        return false;
                    }

                    if (newValue < lastValue) {
                        LOGGER.error("Cached gauge returned stale value, last: {}, new: {}", lastValue, newValue);
                        return false;
                    }

                    lastValue = newValue;
                } while (System.currentTimeMillis() - startTime <= runningTimeMillis);

                return true;
            });

            futures.add(future);
        }

        for (int i = 0; i < futures.size(); i++) {
            assertTrue("Future " + i + " failed", futures.get(i).get());
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }
}
