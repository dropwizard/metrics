package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.MetricsRegistry;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CounterTest {
    private final MetricsRegistry registry = new MetricsRegistry();
    private final Counter counter = registry.newCounter(CounterTest.class, "counter");

    @Test
    public void startsAtZero() throws Exception {
        assertThat("the counter's initial value is zero",
                   counter.getCount(),
                   is(0L));
    }

    @Test
    public void incrementsByOne() throws Exception {
        counter.inc();

        assertThat("the counter's value after being incremented is one",
                   counter.getCount(),
                   is(1L));
    }

    @Test
    public void incrementsByAnArbitraryDelta() throws Exception {
        counter.inc(12);

        assertThat("the counter's value after being incremented by 12 is 12",
                   counter.getCount(),
                   is(12L));
    }

    @Test
    public void decrementsByOne() throws Exception {
        counter.dec();

        assertThat("the counter's value after being decremented is negative one",
                   counter.getCount(),
                   is(-1L));
    }

    @Test
    public void decrementsByAnArbitraryDelta() throws Exception {
        counter.dec(12);

        assertThat("the counter's value after being decremented by 12 is -12",
                   counter.getCount(),
                   is(-12L));
    }

    @Test
    public void isZeroAfterBeingCleared() throws Exception {
        counter.inc(3);
        counter.clear();

        assertThat("the counter's value after being cleared is zero",
                   counter.getCount(),
                   is(0L));
    }
}
