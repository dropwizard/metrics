package com.yammer.metrics.tests;

import com.yammer.metrics.CachedGauge;
import com.yammer.metrics.Gauge;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fest.assertions.api.Assertions.assertThat;

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
}
