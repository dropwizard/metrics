package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.stats.Snapshot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HistogramTest {
    private MetricsRegistry registry;
    private Histogram histogram;

    @Before
    public void setUp() throws Exception {
        this.registry = new MetricsRegistry();
        this.histogram = registry.newHistogram(HistogramTest.class, "histogram", false);
    }

    @After
    public void tearDown() throws Exception {
        registry.shutdown();
    }

    @Test
    public void anEmptyHistogram() throws Exception {
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
        
        assertThat("the histogram has a sum of zero",
                   histogram.sum(),
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
                   histogram.count(),
                   is(1000L));

        assertThat("the histogram has a max of 1000",
                   histogram.max(),
                   is(closeTo(1000.0, 0.0001)));

        assertThat("the histogram has a min of 1",
                   histogram.min(),
                   is(closeTo(1.0, 0.0001)));

        assertThat("the histogram has a mean of 500.5",
                   histogram.mean(),
                   is(closeTo(500.5, 0.0001)));

        assertThat("the histogram has a standard deviation of 288.82",
                   histogram.stdDev(),
                   is(closeTo(288.8194360957494, 0.0001)));
        
        assertThat("the histogram has a sum of 500500",
                   histogram.sum(),
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

    @SuppressWarnings("unchecked")
    @Test
    public void isProcessedAsAHistogram() throws Exception {
        final MetricName name = new MetricName(HistogramTest.class, "histogram");
        final Object context = new Object();
        final MetricProcessor<Object> processor = mock(MetricProcessor.class);

        histogram.processWith(processor, name, context);

        verify(processor).processHistogram(name, histogram, context);
    }
}
