package com.yammer.metrics.tests;

import com.yammer.metrics.Histogram;
import com.yammer.metrics.Snapshot;
import com.yammer.metrics.UniformSample;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.offset;

public class HistogramTest {
    private final Histogram histogram = new Histogram(new UniformSample());

    @Test
    public void anEmptyHistogram() throws Exception {
        assertThat(histogram.getCount())
                .isEqualTo(0L);

        assertThat(histogram.getMax())
                .isEqualTo(0);

        assertThat(histogram.getMin())
                .isEqualTo(0);

        assertThat(histogram.getMean())
                .isEqualTo(0.0, offset(0.0001));

        assertThat(histogram.getStdDev())
                .isEqualTo(0.0, offset(0.0001));

        assertThat(histogram.getSum())
                .isEqualTo(0);

        final Snapshot snapshot = histogram.getSnapshot();

        assertThat(snapshot.getMedian())
                .isEqualTo(0.0, offset(0.0001));

        assertThat(snapshot.get75thPercentile())
                .isEqualTo(0.0, offset(0.0001));

        assertThat(snapshot.get99thPercentile())
                .isEqualTo(0.0, offset(0.0001));

        assertThat(snapshot.size())
                .isEqualTo(0);
    }

    @Test
    public void aHistogramWith1000Elements() throws Exception {
        for (int i = 1; i <= 1000; i++) {
            histogram.update(i);
        }

        assertThat(histogram.getCount())
                .isEqualTo(1000L);

        assertThat(histogram.getMax())
                .isEqualTo(1000);

        assertThat(histogram.getMin())
                .isEqualTo(1);

        assertThat(histogram.getMean())
                .isEqualTo(500.5, offset(0.0001));

        assertThat(histogram.getStdDev())
                .isEqualTo(288.8194360957494, offset(0.0001));

        assertThat(histogram.getSum())
                .isEqualTo(500500);

        final Snapshot snapshot = histogram.getSnapshot();

        assertThat(snapshot.getMedian()).isEqualTo(500.5, offset(0.0001));

        assertThat(snapshot.get75thPercentile()).isEqualTo(750.75, offset(0.0001));

        assertThat(snapshot.get99thPercentile()).isEqualTo(990.99, offset(0.0001));

        assertThat(snapshot.size())
                .isEqualTo(1000);
    }
}
