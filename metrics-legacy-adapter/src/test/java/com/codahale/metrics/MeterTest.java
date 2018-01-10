package com.codahale.metrics;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class MeterTest {

    @Test
    public void testCreateMeteer() {
        Meter meter = new Meter();
        assertThat(meter.getCount()).isEqualTo(0);
    }

    @Test
    public void testCreateMeterWithCustomClock() {
        Meter meter = new Meter(new Clock() {
            @Override
            public long getTick() {
                return 0;
            }
        });
        assertThat(meter.getCount()).isEqualTo(0);
    }

    @Test
    public void testMark() {
        Meter meter = new Meter(new Clock() {

            private long start = System.nanoTime();

            @Override
            public long getTick() {
                return start += TimeUnit.SECONDS.toNanos(1);
            }
        });
        for (int i = 0; i < 60; i++) {
            meter.mark();
        }
        for (int i = 0; i < 60; i++) {
            meter.mark(2);
        }

        assertThat(meter.getCount()).isEqualTo(180);
        assertThat(meter.getFifteenMinuteRate()).isBetween(1.0, 2.0);
        assertThat(meter.getFiveMinuteRate()).isBetween(1.0, 2.0);
        assertThat(meter.getOneMinuteRate()).isBetween(1.0, 2.0);
        assertThat(meter.getMeanRate()).isBetween(1.0, 2.0);
    }
}
