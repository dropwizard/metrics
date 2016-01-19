/*
 * Copyright (C) 2007-2016, GoodData(R) Corporation. All rights reserved.
 */

package io.dropwizard.metrics.jetty9;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.dropwizard.metrics.Metric;
import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;

public class InstrumentedQueuedThreadPoolTest {
    private static final String PREFIX = "prefix";

    private final MetricRegistry metricRegistry = mock(MetricRegistry.class);
    private final InstrumentedQueuedThreadPool iqtp = new InstrumentedQueuedThreadPool(metricRegistry);
    private final ArgumentCaptor<MetricName> metricNameCaptor = ArgumentCaptor.forClass(MetricName.class);

    @Test
    public void customMetricsPrefix() throws Exception{
        iqtp.setPrefix(PREFIX);
        iqtp.doStart();
        verify(metricRegistry, atLeastOnce()).register(metricNameCaptor.capture(), any(Metric.class));
        MetricName metricName = metricNameCaptor.getValue();
        assertThat("Custom metric's prefix doesn't match", metricName.getKey(), startsWith(PREFIX));

    }

    @Test
    public void metricsPrefixBackwardCompatible() throws Exception{
        iqtp.doStart();
        verify(metricRegistry, atLeastOnce()).register(metricNameCaptor.capture(), any(Metric.class));
        MetricName metricName = metricNameCaptor.getValue();
        assertThat("The default metrics prefix was changed", metricName.getKey(), startsWith(QueuedThreadPool.class.getName()));
    }

}
