package com.yammer.metrics.stats.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.stats.ExponentiallyDecayingSample;
import com.yammer.metrics.stats.Snapshot;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ExponentiallyDecayingSampleTest {
    @Test
    @SuppressWarnings("unchecked")
    public void aSampleOf100OutOf1000Elements() throws Exception {
        final ExponentiallyDecayingSample sample = new ExponentiallyDecayingSample(100, 0.99);
        for (int i = 0; i < 1000; i++) {
            sample.update(i);
        }

        assertThat("the sample has a size of 100",
                   sample.size(),
                   is(100));

        final Snapshot snapshot = sample.getSnapshot();

        assertThat("the sample has 100 elements",
                   snapshot.size(),
                   is(100));

        for (double i : snapshot.getValues()) {
            assertThat("the sample only contains elements from the population",
                       i,
                       is(allOf(
                               lessThan(1000.0),
                               greaterThanOrEqualTo(0.0)
                       )));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aSampleOf100OutOf10Elements() throws Exception {
        final ExponentiallyDecayingSample sample = new ExponentiallyDecayingSample(100, 0.99);
        for (int i = 0; i < 10; i++) {
            sample.update(i);
        }

        final Snapshot snapshot = sample.getSnapshot();

        assertThat("the sample has a size of 10",
                   snapshot.size(),
                   is(10));

        assertThat("the sample has 10 elements",
                   snapshot.size(),
                   is(10));

        for (double i : snapshot.getValues()) {
            assertThat("the sample only contains elements from the population",
                       i,
                       is(allOf(
                               lessThan(10.0),
                               greaterThanOrEqualTo(0.0)
                       )));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aHeavilyBiasedSampleOf100OutOf1000Elements() throws Exception {
        final ExponentiallyDecayingSample sample = new ExponentiallyDecayingSample(1000, 0.01);
        for (int i = 0; i < 100; i++) {
            sample.update(i);
        }


        assertThat("the sample has a size of 100",
                   sample.size(),
                   is(100));

        final Snapshot snapshot = sample.getSnapshot();

        assertThat("the sample has 100 elements",
                   snapshot.size(),
                   is(100));

        for (double i : snapshot.getValues()) {
            assertThat("the sample only contains elements from the population",
                       i,
                       is(allOf(
                               lessThan(100.0),
                               greaterThanOrEqualTo(0.0)
                       )));
        }
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
        assertThat("the sample has 10 elements", sample.getSnapshot().size(), is(10));
        assertAllValuesBetween(sample, 1000, 2000);

        // wait for 15 hours and add another value.
        // this should trigger a rescale. Note that the number of samples will be reduced to 2
        // because of the very small scaling factor that will make all existing priorities equal to
        // zero after rescale.
        clock.addHours(15);
        sample.update(2000);
        assertThat("the sample has 2 elements", sample.getSnapshot().size(), is(2));
        assertAllValuesBetween(sample, 1000, 3000);


        // add 1000 values at a rate of 10 values/second
        for (int i = 0; i < 1000; i++) {
            sample.update(3000 + i);
            clock.addMillis(100);
        }
        assertThat("the sample has 10 elements", sample.getSnapshot().size(), is(10));
        assertAllValuesBetween(sample, 3000, 4000);


    }

    @SuppressWarnings("unchecked")
    private void assertAllValuesBetween(ExponentiallyDecayingSample sample,
                                        double min, double max) {
        for (double i : sample.getSnapshot().getValues()) {
            assertThat("the sample only contains elements from the population",
                       i,
                       is(allOf(
                               lessThan(max),
                               greaterThanOrEqualTo(min)
                       )));
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
        public long tick() {
            return ticksInNanos;
        }

        @Override
        public long time() {
            return TimeUnit.NANOSECONDS.toMillis(ticksInNanos);
        }

    }

}
