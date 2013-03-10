package com.yammer.metrics.tests;

import com.yammer.metrics.UniformSample;
import com.yammer.metrics.Snapshot;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class UniformSampleTest {
    @Test
    @SuppressWarnings("unchecked")
    public void aSampleOf100OutOf1000Elements() throws Exception {
        final UniformSample sample = new UniformSample(100);
        for (int i = 0; i < 1000; i++) {
            sample.update(i);
        }

        final Snapshot snapshot = sample.getSnapshot();

        assertThat(sample.size())
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
