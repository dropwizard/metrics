package com.codahale.metrics;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class DeltaCounterTest {
    private final DeltaCounter counter = new DeltaCounter();

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
    public void getAndReset() throws Exception {
        counter.inc(12);

        assertThat(counter.getAndReset())
                .isEqualTo(12);
        assertThat(counter.getCount())
                .isEqualTo(0);
        assertThat(counter.getAndReset())
                .isEqualTo(0);
    }
}
