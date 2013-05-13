package com.codahale.metrics.httpclient;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class InstrumentedHttpClientTest {
    private final HttpClientMetricNameStrategy metricNameStrategy =
            mock(HttpClientMetricNameStrategy.class);
    private final MetricRegistryListener registryListener =
            mock(MetricRegistryListener.class);
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final HttpClient client =
            new InstrumentedHttpClient(metricRegistry, metricNameStrategy);

    @Before
    public void setUp() throws Exception {
        metricRegistry.addListener(registryListener);
    }

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        final HttpGet get = new HttpGet("http://example.com?q=anything");
        final String metricName = "some.made.up.metric.name";

        when(metricNameStrategy.getNameFor(anyString(), eq(get)))
                .thenReturn(metricName);

        client.execute(get);

        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }
}
