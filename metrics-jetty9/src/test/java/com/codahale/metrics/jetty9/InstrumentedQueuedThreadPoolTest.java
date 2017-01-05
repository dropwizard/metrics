package com.codahale.metrics.jetty9;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class InstrumentedQueuedThreadPoolTest {
    private static final String PREFIX = "prefix";

    private final MetricRegistry metricRegistry = mock(MetricRegistry.class);
    private final InstrumentedQueuedThreadPool iqtp = new InstrumentedQueuedThreadPool(metricRegistry);
    private final ArgumentCaptor<String> metricNameCaptor = ArgumentCaptor.forClass(String.class);

    @After
    public void tearDown() throws Exception {
        iqtp.stop();
    }

    @Test
    public void customMetricsPrefix() throws Exception{
        iqtp.setPrefix(PREFIX);
        iqtp.doStart();

        verify(metricRegistry, atLeastOnce()).register(metricNameCaptor.capture(), any(Metric.class));
        String metricName = metricNameCaptor.getValue();
        assertThat("Custom metric's prefix doesn't match", metricName, startsWith(PREFIX));

    }

    @Test
    public void metricsPrefixBackwardCompatible() throws Exception{
        iqtp.doStart();

        verify(metricRegistry, atLeastOnce()).register(metricNameCaptor.capture(), any(Metric.class));
        String metricName = metricNameCaptor.getValue();
        assertThat("The default metrics prefix was changed", metricName, startsWith(QueuedThreadPool.class.getName()));
    }

}
