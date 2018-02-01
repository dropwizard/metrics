package com.codahale.metrics;

import io.dropwizard.metrics5.Counter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class CounterTest {

    private Counter counter = new Counter();

    @Test
    public void testIncrementCounter() {
        counter.inc();

        assertThat(counter.getCount()).isEqualTo(1);
    }

    @Test
    public void testIncrementCounterOnManyPoints() {
        counter.inc(5);

        assertThat(counter.getCount()).isEqualTo(5);
    }

    @Test
    public void testDecrementCounter() {
        counter.dec();

        assertThat(counter.getCount()).isEqualTo(-1);
    }

    @Test
    public void testDecrementCounterOnManyPoints() {
        counter.dec(5);

        assertThat(counter.getCount()).isEqualTo(-5);
    }
}
