package com.yammer.metrics.stats.tests;

import com.yammer.metrics.stats.Snapshot;
import com.yammer.metrics.stats.UniformSample;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class UniformSampleTest {
    @Test
    @SuppressWarnings("unchecked")
    public void aSampleOf100OutOf1000Elements() throws Exception {
        final UniformSample sample = new UniformSample(100);
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

}
