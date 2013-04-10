package com.yammer.metrics.httpclient.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistryListener;
import com.yammer.metrics.httpclient.strategies.HttpClientMetricNameStrategy;
import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import com.yammer.metrics.httpclient.InstrumentedHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstrumentedHttpClientTest {

    @Mock
    public HttpClientMetricNameStrategy metricNameStrategy;
    @Mock
    public MetricsRegistryListener registryListener;

    private HttpClient client;

    @Before
    public void init() {
        client = new InstrumentedHttpClient(metricNameStrategy);
        Metrics.defaultRegistry().addListener(registryListener);
    }

    @Test
    public void hasAnInstrumentedConnectionManager() throws Exception {
        assertThat(client.getConnectionManager(),
                   is(instanceOf(InstrumentedClientConnManager.class)));
    }

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        HttpGet get = new HttpGet("http://google.com?q=anything");
        MetricName metricName = new MetricName(getClass(), "test-get-metric");

        when(metricNameStrategy.getNameFor(get)).thenReturn(metricName);

        try {
            client.execute(get);
        } catch (Exception e) {
        }

        verify(registryListener).onMetricAdded(eq(metricName), any(Metric.class));
    }
}
