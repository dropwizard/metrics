package io.dropwizard.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullCounterTest {

    @Test
    public void startsAtZero() throws Exception {
        assertThat(new NullCounter().getCount())
                .isZero();
    }
    
    @Test
    public void startsAtValue() throws Exception {
        assertThat(new NullCounter(100).getCount())
                .isEqualTo(100);
    }

    @Test
    public void incrementDoesNothing() throws Exception {
        Counter counter = new NullCounter(100);
        counter.inc();

        assertThat(counter.getCount())
                .isEqualTo(100);
    }

    @Test
    public void incrementsByAnArbitraryDeltaDoesNothing() throws Exception {
        Counter counter = new NullCounter(100);
        counter.inc(12);

        assertThat(counter.getCount())
                .isEqualTo(100);
    }

    @Test
    public void decrementsByOneDoesNothing() throws Exception {
        Counter counter = new NullCounter(100);
        counter.dec();

        assertThat(counter.getCount())
                .isEqualTo(100);
    }

    @Test
    public void decrementsByAnArbitraryDeltaDoesNothing() throws Exception {
        Counter counter = new NullCounter(100);
        counter.dec(12);

        assertThat(counter.getCount())
                .isEqualTo(100);
    }

}
