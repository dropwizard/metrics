package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.stats.UniformSample;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HistogramMetricTest {
    @Test
    public void anEmptyHistogram() throws Exception {
        final HistogramMetric histogram = new HistogramMetric(new UniformSample(100));

        assertThat("the histogram has a count of zero",
                   histogram.count(),
                   is(0L));

        assertThat("the histogram has a max of zero",
                   histogram.max(),
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a min of zero",
                   histogram.min(),
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a mean of zero",
                   histogram.mean(),
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a standard deviation of zero",
                   histogram.stdDev(),
                   is(closeTo(0.0, 0.0001)));

        final Double[] percentiles = histogram.percentiles(0.5, 0.75, 0.99);

        assertThat("the histogram has a median of zero",
                   percentiles[0],
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a 75th percentile of zero",
                   percentiles[1],
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a 99th percentile of zero",
                   percentiles[2],
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram is empty",
                   histogram.values().isEmpty(),
                   is(true));
    }

    @Test
    public void aHistogramWith10000Elements() throws Exception {
        final HistogramMetric histogram = new HistogramMetric(new UniformSample(100000));
        for (int i = 1; i <= 10000; i++) {
            histogram.update(i);
        }

        assertThat("the histogram has a count of zero",
                   histogram.count(),
                   is(10000L));

        assertThat("the histogram has a max of 10000",
                   histogram.max(),
                   is(closeTo(10000.0, 0.0001)));

        assertThat("the histogram has a min of 1",
                   histogram.min(),
                   is(closeTo(1.0, 0.0001)));

        assertThat("the histogram has a mean of 5000.5",
                   histogram.mean(),
                   is(closeTo(5000.5, 0.0001)));

        assertThat("the histogram has a standard deviation of 2886.89",
                   histogram.stdDev(),
                   is(closeTo(2886.8956799071675, 0.0001)));

        final Double[] percentiles = histogram.percentiles(0.5, 0.75, 0.99);

        assertThat("the histogram has a median of 5000.5",
                   percentiles[0],
                   is(closeTo(5000.5, 0.0001)));

        assertThat("the histogram has a 75th percentile of 7500.75",
                   percentiles[1],
                   is(closeTo(7500.75, 0.0001)));

        assertThat("the histogram has a 99th percentile of 9900.99",
                   percentiles[2],
                   is(closeTo(9900.99, 0.0001)));

        assertThat("the histogram is has 10000 values",
                   histogram.values().size(),
                   is(10000));
    }

}
