package io.dropwizard.metrics.httpasyncclient;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.httpclient.HttpClientMetricNameStrategy;

import io.dropwizard.metrics.httpasyncclient.InstrumentedNHttpClientBuilder;
import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.MetricRegistryListener;
import io.dropwizard.metrics.Timer;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class InstrumentedHttpClientsTest {
    private final HttpClientMetricNameStrategy metricNameStrategy =
            mock(HttpClientMetricNameStrategy.class);
    private final MetricRegistryListener registryListener =
            mock(MetricRegistryListener.class);
    private final MetricRegistry metricRegistry = new MetricRegistry();

    private  HttpAsyncClient hac;

    @Before
    public void setUp() throws Exception {
        CloseableHttpAsyncClient chac = new InstrumentedNHttpClientBuilder(metricRegistry, metricNameStrategy).build();
        chac.start();
        hac = chac;
        metricRegistry.addListener(registryListener);
    }

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        final HttpGet get = new HttpGet("http://example.com?q=anything");
        final MetricName metricName = MetricName.build("some.made.up.metric.name");

        when(metricNameStrategy.getNameFor(anyString(), any(HttpRequest.class)))
                .thenReturn(metricName);

        hac.execute(get,null).get();

        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }
}
