package com.yammer.metrics.tests;

import com.yammer.metrics.Clock;
import com.yammer.metrics.ExponentiallyDecayingSample;
import com.yammer.metrics.Snapshot;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

public class ExponentiallyDecayingSampleTest {
    @Test
    public void aSampleOf100OutOf1000Elements() throws Exception {
        final ExponentiallyDecayingSample sample = new ExponentiallyDecayingSample(100, 0.99);
        for (int i = 0; i < 1000; i++) {
            sample.update(i);
        }

        assertThat(sample.size())
                .isEqualTo(100);

        final Snapshot snapshot = sample.getSnapshot();

        assertThat(snapshot.size())
                .isEqualTo(100);

        assertAllValuesBetween(sample, 0, 1000);
    }

    @Test
    public void aSampleOf100OutOf10Elements() throws Exception {
        final ExponentiallyDecayingSample sample = new ExponentiallyDecayingSample(100, 0.99);
        for (int i = 0; i < 10; i++) {
            sample.update(i);
        }

        final Snapshot snapshot = sample.getSnapshot();

        assertThat(snapshot.size())
                .isEqualTo(10);

        assertThat(snapshot.size())
                .isEqualTo(10);

        assertAllValuesBetween(sample, 0, 10);
    }

    @Test
    public void aHeavilyBiasedSampleOf100OutOf1000Elements() throws Exception {
        final ExponentiallyDecayingSample sample = new ExponentiallyDecayingSample(1000, 0.01);
        for (int i = 0; i < 100; i++) {
            sample.update(i);
        }


        assertThat(sample.size())
                .isEqualTo(100);

        final Snapshot snapshot = sample.getSnapshot();

        assertThat(snapshot.size())
                .isEqualTo(100);

        assertAllValuesBetween(sample, 0, 100);
    }

    @Test
    public void longPeriodsOfInactivityShouldNotCorruptSamplingState() {
        final ManualClock clock = new ManualClock();
        final ExponentiallyDecayingSample sample = new ExponentiallyDecayingSample(10,
                                                                                   0.015,
                                                                                   clock);

        // add 1000 values at a rate of 10 values/second
        for (int i = 0; i < 1000; i++) {
            sample.update(1000 + i);
            clock.addMillis(100);
        }
        assertThat(sample.getSnapshot().size())
                .isEqualTo(10);
        assertAllValuesBetween(sample, 1000, 2000);

        // wait for 15 hours and add another value.
        // this should trigger a rescale. Note that the number of samples will be reduced to 2
        // because of the very small scaling factor that will make all existing priorities equal to
        // zero after rescale.
        clock.addHours(15);
        sample.update(2000);
        assertThat(sample.getSnapshot().size())
                .isEqualTo(2);
        assertAllValuesBetween(sample, 1000, 3000);


        // add 1000 values at a rate of 10 values/second
        for (int i = 0; i < 1000; i++) {
            sample.update(3000 + i);
            clock.addMillis(100);
        }
        assertThat(sample.getSnapshot().size())
                .isEqualTo(10);
        assertAllValuesBetween(sample, 3000, 4000);
    }

    private static void assertAllValuesBetween(ExponentiallyDecayingSample sample,
                                        double min, double max) {
        for (double i : sample.getSnapshot().getValues()) {
            assertThat(i)
                    .isLessThan(max)
                    .isGreaterThanOrEqualTo(min);
        }
    }

    class ManualClock extends Clock {
        long ticksInNanos = 0;

        public void addMillis(long millis) {
            ticksInNanos += TimeUnit.MILLISECONDS.toNanos(millis);
        }

        public void addHours(long hours) {
            ticksInNanos += TimeUnit.HOURS.toNanos(hours);
        }

        @Override
        public long getTick() {
            return ticksInNanos;
        }

        @Override
        public long getTime() {
            return TimeUnit.NANOSECONDS.toMillis(ticksInNanos);
        }
    }
}
