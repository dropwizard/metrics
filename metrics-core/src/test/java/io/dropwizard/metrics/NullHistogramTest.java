package io.dropwizard.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullHistogramTest {

    @Test
    public void startsAtZero() throws Exception {
        Histogram histogram = new NullHistogram();
        assertThat(histogram.getCount())
                .isEqualTo(1);
        assertThat(histogram.getSnapshot().get75thPercentile())
                .isZero();
        assertThat(histogram.getSnapshot().get95thPercentile())
                .isZero();
    }

    @Test
    public void updatesDoNothing() throws Exception {
        Histogram histogram = new NullHistogram(10);
        assertThat(histogram.getCount())
                .isEqualTo(1);

        histogram.update(1);

        assertThat(histogram.getSnapshot().get98thPercentile())
                .isEqualTo(10);
        assertThat(histogram.getSnapshot().get99thPercentile())
                .isEqualTo(10);
        assertThat(histogram.getSnapshot().get999thPercentile())
                .isEqualTo(10);
        assertThat(histogram.getSnapshot().getMax())
                .isEqualTo(10);
        assertThat(histogram.getSnapshot().getMin())
                .isEqualTo(10);
        assertThat(histogram.getSnapshot().getMean())
                .isEqualTo(10);
        assertThat(histogram.getSnapshot().getMedian())
                .isEqualTo(10);
    }

}
