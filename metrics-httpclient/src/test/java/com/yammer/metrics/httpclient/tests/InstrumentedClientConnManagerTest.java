package com.yammer.metrics.httpclient.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistryListener;
import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InstrumentedClientConnManagerTest {

    @Mock
    public MetricsRegistryListener registryListener;

    @Before
    public void init() {
        Metrics.defaultRegistry().addListener(registryListener);
    }

    @Test
    public void withDefaultDomain() {
        new InstrumentedClientConnManager();

        MetricName metricName = new MetricName("org.apache.http.conn", "ClientConnectionManager", "leased-connections");
        verify(registryListener).onMetricAdded(eq(metricName), any(Metric.class));
    }

    @Test
    public void supportsOverriddenDomain() {
        new InstrumentedClientConnManager("my-domain");

        MetricName metricName = new MetricName("my-domain", "ClientConnectionManager", "leased-connections");
        verify(registryListener).onMetricAdded(eq(metricName), any(Metric.class));
    }
}
