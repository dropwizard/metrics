package com.codahale.metrics.httpasyncclient;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstrumentedHttpClientsTest extends HttpClientTestBase {

    private final MetricRegistry metricRegistry = new MetricRegistry();

    private HttpAsyncClient asyncHttpClient;
    private HttpClientMetricNameStrategy metricNameStrategy = mock(HttpClientMetricNameStrategy.class);
    private MetricRegistryListener registryListener = mock(MetricRegistryListener.class);

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        final HttpGet get = new HttpGet("/q=anything");
        final String metricName = MetricRegistry.name("some.made.up.metric.name");

        when(metricNameStrategy.getNameFor(any(), any(HttpRequest.class)))
                .thenReturn(metricName);

        asyncHttpClient.execute(host, get, null).get();

        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }

    @BeforeEach
    public void setUp() throws Exception {
        CloseableHttpAsyncClient chac = new InstrumentedNHttpClientBuilder(metricRegistry, metricNameStrategy).build();
        chac.start();
        asyncHttpClient = chac;
        metricRegistry.addListener(registryListener);
    }
}
