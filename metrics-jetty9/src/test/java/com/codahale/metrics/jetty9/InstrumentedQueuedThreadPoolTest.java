package com.codahale.metrics.jetty9;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
    public void customMetricsPrefix() throws Exception {
        iqtp.setPrefix(PREFIX);
        iqtp.doStart();

        verify(metricRegistry, atLeastOnce()).register(metricNameCaptor.capture(), any(Metric.class));
        String metricName = metricNameCaptor.getValue();
        assertThat(metricName)
                .overridingErrorMessage("Custom metric's prefix doesn't match")
                .startsWith(PREFIX);

    }

    @Test
    public void metricsPrefixBackwardCompatible() throws Exception {
        iqtp.doStart();

        verify(metricRegistry, atLeastOnce()).register(metricNameCaptor.capture(), any(Metric.class));
        String metricName = metricNameCaptor.getValue();
        assertThat(metricName)
                .overridingErrorMessage("The default metrics prefix was changed")
                .startsWith(QueuedThreadPool.class.getName());
    }

}
