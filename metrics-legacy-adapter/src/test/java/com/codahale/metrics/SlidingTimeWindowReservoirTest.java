package com.codahale.metrics;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class SlidingTimeWindowReservoirTest {

    @Test
    void testCreateWithWindow() {
        SlidingTimeWindowReservoir reservoir = new SlidingTimeWindowReservoir(1, TimeUnit.HOURS);
        reservoir.update(100);
        reservoir.update(200);
        reservoir.update(30);

        assertThat(reservoir.size()).isEqualTo(3);
        assertThat(reservoir.getSnapshot().getMean()).isCloseTo(110, Offset.offset(0.1));
    }

    @Test
    void testCreateWithWindowAndClock() {
        SlidingTimeWindowReservoir reservoir = new SlidingTimeWindowReservoir(1, TimeUnit.HOURS,
                new Clock.UserTimeClock());
        reservoir.update(400);
        reservoir.update(300);

        assertThat(reservoir.size()).isEqualTo(2);
        assertThat(reservoir.getSnapshot().getMean()).isCloseTo(350, Offset.offset(0.1));
    }
}
