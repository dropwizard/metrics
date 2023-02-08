package com.codahale.metrics;

import io.dropwizard.metrics5.Counter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class CounterTest {

    private Counter counter = new Counter();

    @Test
    void testIncrementCounter() {
        counter.inc();

        assertThat(counter.getCount()).isEqualTo(1);
    }

    @Test
    void testIncrementCounterOnManyPoints() {
        counter.inc(5);

        assertThat(counter.getCount()).isEqualTo(5);
    }

    @Test
    void testDecrementCounter() {
        counter.dec();

        assertThat(counter.getCount()).isEqualTo(-1);
    }

    @Test
    void testDecrementCounterOnManyPoints() {
        counter.dec(5);

        assertThat(counter.getCount()).isEqualTo(-5);
    }
}
