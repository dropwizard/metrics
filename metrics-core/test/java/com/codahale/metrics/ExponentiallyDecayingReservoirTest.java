package com.codahale.metrics;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

public class ExponentiallyDecayingReservoirTest {
    @Test
    public void aReservoirOf100OutOf1000Elements() throws Exception {
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(100, 0.99);
        for (int i = 0; i < 1000; i++) {
            reservoir.update(i);
        }

        assertThat(reservoir.size())
                .isEqualTo(100);

        final Snapshot snapshot = reservoir.getSnapshot();

        assertThat(snapshot.size())
                .isEqualTo(100);

        assertAllValuesBetween(reservoir, 0, 1000);
    }

    @Test
    public void aReservoirOf100OutOf10Elements() throws Exception {
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(100, 0.99);
        for (int i = 0; i < 10; i++) {
            reservoir.update(i);
        }

        final Snapshot snapshot = reservoir.getSnapshot();

        assertThat(snapshot.size())
                .isEqualTo(10);

        assertThat(snapshot.size())
                .isEqualTo(10);

        assertAllValuesBetween(reservoir, 0, 10);
    }

    @Test
    public void aHeavilyBiasedReservoirOf100OutOf1000Elements() throws Exception {
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(1000, 0.01);
        for (int i = 0; i < 100; i++) {
            reservoir.update(i);
        }


        assertThat(reservoir.size())
                .isEqualTo(100);

        final Snapshot snapshot = reservoir.getSnapshot();

        assertThat(snapshot.size())
                .isEqualTo(100);

        assertAllValuesBetween(reservoir, 0, 100);
    }

    @Test
    public void longPeriodsOfInactivityShouldNotCorruptSamplingState() {
        final ManualClock clock = new ManualClock();
        final ExponentiallyDecayingReservoir reservoir = new ExponentiallyDecayingReservoir(10,
                                                                                            0.015,
                                                                                            clock);

        // add 1000 values at a rate of 10 values/second
        for (int i = 0; i < 1000; i++) {
            reservoir.update(1000 + i);
            clock.addMillis(100);
        }
        assertThat(reservoir.getSnapshot().size())
                .isEqualTo(10);
        assertAllValuesBetween(reservoir, 1000, 2000);

        // wait for 15 hours and add another value.
        // this should trigger a rescale. Note that the number of samples will be reduced to 2
        // because of the very small scaling factor that will make all existing priorities equal to
        // zero after rescale.
        clock.addHours(15);
        reservoir.update(2000);
        assertThat(reservoir.getSnapshot().size())
                .isEqualTo(2);
        assertAllValuesBetween(reservoir, 1000, 3000);


        // add 1000 values at a rate of 10 values/second
        for (int i = 0; i < 1000; i++) {
            reservoir.update(3000 + i);
            clock.addMillis(100);
        }
        assertThat(reservoir.getSnapshot().size())
                .isEqualTo(10);
        assertAllValuesBetween(reservoir, 3000, 4000);
    }

    private static void assertAllValuesBetween(ExponentiallyDecayingReservoir reservoir,
                                               double min,
                                               double max) {
        for (double i : reservoir.getSnapshot().getValues()) {
            assertThat(i)
                    .isLessThan(max)
                    .isGreaterThanOrEqualTo(min);
        }
    }

}
