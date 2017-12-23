package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CounterTest {
    private final Counter counter = new Counter();

    @Test
    public void startsAtZero() {
        assertThat(counter.getCount())
                .isZero();
    }

    @Test
    public void incrementsByOne() {
        counter.inc();

        assertThat(counter.getCount())
                .isEqualTo(1);
    }

    @Test
    public void incrementsByAnArbitraryDelta() {
        counter.inc(12);

        assertThat(counter.getCount())
                .isEqualTo(12);
    }

    @Test
    public void decrementsByOne() {
        counter.dec();

        assertThat(counter.getCount())
                .isEqualTo(-1);
    }

    @Test
    public void decrementsByAnArbitraryDelta() {
        counter.dec(12);

        assertThat(counter.getCount())
                .isEqualTo(-12);
    }

    @Test
    public void incrementByNegativeDelta() {
        counter.inc(-12);

        assertThat(counter.getCount())
                .isEqualTo(-12);
    }

    @Test
    public void decrementByNegativeDelta() {
        counter.dec(-12);

        assertThat(counter.getCount())
                .isEqualTo(12);
    }
}
