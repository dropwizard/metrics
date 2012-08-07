package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.stats.Snapshot;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HistogramTest {
    private final MetricsRegistry registry = new MetricsRegistry();
    private final Histogram histogram = registry.newHistogram(HistogramTest.class, "histogram", false);

    @Test
    public void anEmptyHistogram() throws Exception {
        assertThat("the histogram has a count of zero",
                   histogram.getCount(),
                   is(0L));

        assertThat("the histogram has a max of zero",
                   histogram.getMax(),
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a min of zero",
                   histogram.getMin(),
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a mean of zero",
                   histogram.getMean(),
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a standard deviation of zero",
                   histogram.getStdDev(),
                   is(closeTo(0.0, 0.0001)));
        
        assertThat("the histogram has a sum of zero",
                   histogram.getSum(),
                   is(closeTo(0.0, 0.0001)));

        final Snapshot snapshot = histogram.getSnapshot();

        assertThat("the histogram has a median of zero",
                   snapshot.getMedian(),
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a 75th percentile of zero",
                   snapshot.get75thPercentile(),
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a 99th percentile of zero",
                   snapshot.get99thPercentile(),
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram is empty",
                   snapshot.size(),
                   is(0));
    }

    @Test
    public void aHistogramWith1000Elements() throws Exception {
        for (int i = 1; i <= 1000; i++) {
            histogram.update(i);
        }

        assertThat("the histogram has a count of 1000",
                   histogram.getCount(),
                   is(1000L));

        assertThat("the histogram has a max of 1000",
                   histogram.getMax(),
                   is(closeTo(1000.0, 0.0001)));

        assertThat("the histogram has a min of 1",
                   histogram.getMin(),
                   is(closeTo(1.0, 0.0001)));

        assertThat("the histogram has a mean of 500.5",
                   histogram.getMean(),
                   is(closeTo(500.5, 0.0001)));

        assertThat("the histogram has a standard deviation of 288.82",
                   histogram.getStdDev(),
                   is(closeTo(288.8194360957494, 0.0001)));
        
        assertThat("the histogram has a sum of 500500",
                   histogram.getSum(),
                   is(closeTo(500500, 0.1)));

        final Snapshot snapshot = histogram.getSnapshot();

        assertThat("the histogram has a median of 500.5",
                   snapshot.getMedian(),
                   is(closeTo(500.5, 0.0001)));

        assertThat("the histogram has a 75th percentile of 750.75",
                   snapshot.get75thPercentile(),
                   is(closeTo(750.75, 0.0001)));

        assertThat("the histogram has a 99th percentile of 990.99",
                   snapshot.get99thPercentile(),
                   is(closeTo(990.99, 0.0001)));

        assertThat("the histogram has 1000 values",
                   snapshot.size(),
                   is(1000));
    }
}
