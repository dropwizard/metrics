package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsProcessor;
import com.yammer.metrics.stats.UniformSample;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HistogramTest {
    @Test
    public void anEmptyHistogram() throws Exception {
        final Histogram histogram = new Histogram(new UniformSample(100));

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

        final Double[] quantiles = histogram.quantiles(0.5, 0.75, 0.99);

        assertThat("the histogram has a median of zero",
                   quantiles[0],
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a 75th percentile of zero",
                   quantiles[1],
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram has a 99th percentile of zero",
                   quantiles[2],
                   is(closeTo(0.0, 0.0001)));

        assertThat("the histogram is empty",
                   histogram.values().isEmpty(),
                   is(true));
    }

    @Test
    public void aHistogramWith10000Elements() throws Exception {
        final Histogram histogram = new Histogram(new UniformSample(100000));
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

        final Double[] quantiles = histogram.quantiles(0.5, 0.75, 0.99);

        assertThat("the histogram has a median of 5000.5",
                   quantiles[0],
                   is(closeTo(5000.5, 0.0001)));

        assertThat("the histogram has a 75th percentile of 7500.75",
                   quantiles[1],
                   is(closeTo(7500.75, 0.0001)));

        assertThat("the histogram has a 99th percentile of 9900.99",
                   quantiles[2],
                   is(closeTo(9900.99, 0.0001)));

        assertThat("the histogram is has 10000 values",
                   histogram.values().size(),
                   is(10000));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void isProcessedAsAHistogram() throws Exception {
        final Histogram histogram = new Histogram(new UniformSample(100000));
        final MetricName name = new MetricName(HistogramTest.class, "histogram");
        final Object context = new Object();
        final MetricsProcessor<Object> processor = mock(MetricsProcessor.class);

        histogram.processWith(processor, name, context);

        verify(processor).processHistogram(name, histogram, context);
    }
}
