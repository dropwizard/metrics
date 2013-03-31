package com.yammer.metrics.tests;

import com.yammer.metrics.SlidingWindowSample;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class SlidingWindowSampleTest {
    private final SlidingWindowSample sample = new SlidingWindowSample(3);

    @Test
    public void handlesSmallSamples() throws Exception {
        sample.update(1);
        sample.update(2);

        assertThat(sample.getSnapshot().getValues())
                .containsOnly(1, 2);
    }

    @Test
    public void onlyKeepsTheMostRecentFromBigSamples() throws Exception {
        sample.update(1);
        sample.update(2);
        sample.update(3);
        sample.update(4);

        assertThat(sample.getSnapshot().getValues())
                .containsOnly(2, 3, 4);
    }
}
