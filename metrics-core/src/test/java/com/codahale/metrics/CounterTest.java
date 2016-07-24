package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CounterTest {
    private final Counter counter = new Counter();

    @Test
    public void startsAtZero() throws Exception {
        assertThat(counter.getCount())
                .isZero();
    }

    @Test
    public void incrementsByOne() throws Exception {
        counter.inc();

        assertThat(counter.getCount())
                .isEqualTo(1);
    }

    @Test
    public void incrementsByAnArbitraryDelta() throws Exception {
        counter.inc(12);

        assertThat(counter.getCount())
                .isEqualTo(12);
    }

    @Test
    public void decrementsByOne() throws Exception {
        counter.dec();

        assertThat(counter.getCount())
                .isEqualTo(-1);
    }

    @Test
    public void decrementsByAnArbitraryDelta() throws Exception {
        counter.dec(12);

        assertThat(counter.getCount())
                .isEqualTo(-12);
    }

    @Test
    public void incrementByNegativeDelta() throws Exception {
        counter.inc(-12);

        assertThat(counter.getCount())
                .isEqualTo(-12);
    }

    @Test
    public void decrementByNegativeDelta() throws Exception {
        counter.dec(-12);

        assertThat(counter.getCount())
                .isEqualTo(12);
    }
}
