package com.codahale.metrics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class MeterTest {

    @Test
    void testCreateMeteer() {
        Meter meter = new Meter();
        assertThat(meter.getCount()).isEqualTo(0);
    }

    @Test
    void testCreateMeterWithCustomClock() {
        Meter meter = new Meter(new Clock() {
            @Override
            public long getTick() {
                return 0;
            }
        });
        assertThat(meter.getCount()).isEqualTo(0);
    }

    @Test
    void testMark() {
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
        assertThat(meter.getMeanRate()).isBetween(1.0, 2.0);
    }
}
