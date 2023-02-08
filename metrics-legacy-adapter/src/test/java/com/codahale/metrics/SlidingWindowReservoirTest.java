package com.codahale.metrics;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class SlidingWindowReservoirTest {

    @Test
    void testCreateWithBigWindow() {
        SlidingWindowReservoir reservoir = new SlidingWindowReservoir(100);
        reservoir.update(100);
        reservoir.update(220);
        reservoir.update(130);

        assertThat(reservoir.size()).isEqualTo(3);
        assertThat(reservoir.getSnapshot().getMean()).isCloseTo(150, Offset.offset(0.1));
    }

    @Test
    void testCreateWithLowWindow() {
        SlidingWindowReservoir reservoir = new SlidingWindowReservoir(3);
        reservoir.update(500);
        reservoir.update(220);
        reservoir.update(100);
        reservoir.update(40);

        assertThat(reservoir.size()).isEqualTo(3);
        assertThat(reservoir.getSnapshot().getMean()).isCloseTo(120, Offset.offset(0.1));
    }
}
