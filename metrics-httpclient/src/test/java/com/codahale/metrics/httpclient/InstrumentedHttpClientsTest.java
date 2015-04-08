package com.codahale.metrics.httpclient;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class InstrumentedHttpClientsTest {
    private final HttpRequestMetricNameStrategy metricNameStrategy =
            mock(HttpRequestMetricNameStrategy.class);
    private final MetricRegistryListener registryListener =
            mock(MetricRegistryListener.class);
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final HttpClient client =
            InstrumentedHttpClients.createDefault(metricRegistry, metricNameStrategy);

    @Before
    public void setUp() throws Exception {
        metricRegistry.addListener(registryListener);
    }

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        final HttpGet get = new HttpGet("http://example.com?q=anything");
        final String metricNameActive = "some.made.up.metric.name.active";
        final String metricNameDuration = "some.made.up.metric.name.duration";


        when(metricNameStrategy.getNameForActive(isA(Class.class), anyString(), any(HttpRequest.class)))
                .thenReturn(metricNameActive);
        when(metricNameStrategy.getNameForDuration(isA(Class.class), anyString(), any(HttpRequest.class)))
                .thenReturn(metricNameDuration);

        client.execute(get);

        verify(registryListener).onCounterAdded(eq(metricNameActive), any(Counter.class));
        verify(registryListener).onTimerAdded(eq(metricNameDuration), any(Timer.class));

    }
}
