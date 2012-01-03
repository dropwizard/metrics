package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GaugeTest {
    final Gauge<String> gauge = new Gauge<String>() {
        @Override
        public String value() {
            return "woo";
        }
    };

    @Test
    public void returnsAValue() throws Exception {
        assertThat("a gauge returns a value",
                   gauge.value(),
                   is("woo"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void isProcessedAsAGauge() throws Exception {
        final MetricName name = new MetricName(GaugeTest.class, "gauge");
        final Object context = new Object();
        final MetricProcessor<Object> processor = mock(MetricProcessor.class);

        gauge.processWith(processor, name, context);

        verify(processor).processGauge(name, gauge, context);
    }
}
