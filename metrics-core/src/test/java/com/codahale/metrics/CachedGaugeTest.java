package com.codahale.metrics;

import junit.framework.Assert;
import org.assertj.core.util.Throwables;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CachedGaugeTest {
    private final AtomicInteger value = new AtomicInteger(0);
    private final Gauge<Integer> gauge = new CachedGauge<Integer>(100, TimeUnit.MILLISECONDS) {
        @Override
        protected Integer loadValue() {
            return value.incrementAndGet();
        }
    };

    @Test
    public void cachesTheValueForTheGivenPeriod() throws Exception {
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
    public void shouldBeNonNullCachedValueOnConcurrentAccess() throws Throwable {
        final CachedGauge<Integer> gauge = new CachedGauge<Integer>(10, TimeUnit.SECONDS) {
            public Integer loadValue() {
                return 10;
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        List<Future<?>> futureList = new ArrayList<Future<?>>();
        for (int i = 0; i < 100; i++) {
            Future<?> future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    assertThat(gauge.getValue()).isNotNull();
                }
            });
            futureList.add(future);
        }

        for (Future<?> future : futureList) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }

        executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }
}
