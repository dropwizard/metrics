package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CounterTest {
    private final Counter counter = new Counter();

    @Test
    void startsAtZero() {
        assertThat(counter.getCount())
                .isZero();
    }

    @Test
    void incrementsByOne() {
        counter.inc();

        assertThat(counter.getCount())
                .isEqualTo(1);
    }

    @Test
    void incrementsByAnArbitraryDelta() {
        counter.inc(12);

        assertThat(counter.getCount())
                .isEqualTo(12);
    }

    @Test
    void decrementsByOne() {
        counter.dec();

        assertThat(counter.getCount())
                .isEqualTo(-1);
    }

    @Test
    void decrementsByAnArbitraryDelta() {
        counter.dec(12);

        assertThat(counter.getCount())
                .isEqualTo(-12);
    }

    @Test
    void incrementByNegativeDelta() {
        counter.inc(-12);

        assertThat(counter.getCount())
                .isEqualTo(-12);
    }

    @Test
    void decrementByNegativeDelta() {
        counter.dec(-12);

        assertThat(counter.getCount())
                .isEqualTo(12);
    }
}
