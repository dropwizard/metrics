package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UniformReservoirTest {
    @Test
    @SuppressWarnings("unchecked")
    public void aReservoirOf100OutOf1000Elements() {
        final UniformReservoir reservoir = new UniformReservoir(100);
        for (int i = 0; i < 1000; i++) {
            reservoir.update(i);
        }

        final Snapshot snapshot = reservoir.getSnapshot();

        assertThat(reservoir.size())
                .isEqualTo(100);

        assertThat(snapshot.size())
                .isEqualTo(100);

        for (double i : snapshot.getValues()) {
            assertThat(i)
                    .isLessThan(1000)
                    .isGreaterThanOrEqualTo(0);
        }
    }

}
