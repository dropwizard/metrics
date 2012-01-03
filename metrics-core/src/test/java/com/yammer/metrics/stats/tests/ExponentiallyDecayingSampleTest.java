package com.yammer.metrics.stats.tests;

import com.yammer.metrics.stats.ExponentiallyDecayingSample;
import com.yammer.metrics.stats.Snapshot;
import org.junit.Test;

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

        final Snapshot snapshot = sample.getSnapshot();

        assertThat("the sample has a size of 100",
                   sample.size(),
                   is(100));

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

        assertThat("the sample has a size of 10",
                   sample.size(),
                   is(10));

        assertThat("the sample has 10 elements",
                   sample.values().size(),
                   is(10));

        for (Long i : sample.values()) {
            assertThat("the sample only contains elements from the population",
                       i,
                       is(allOf(
                               lessThan(10L),
                               greaterThanOrEqualTo(0L)
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

        assertThat("the sample has 100 elements",
                   sample.values().size(),
                   is(100));

        for (Long i : sample.values()) {
            assertThat("the sample only contains elements from the population",
                       i,
                       is(allOf(
                               lessThan(100L),
                               greaterThanOrEqualTo(0L)
                       )));
        }
    }
}
