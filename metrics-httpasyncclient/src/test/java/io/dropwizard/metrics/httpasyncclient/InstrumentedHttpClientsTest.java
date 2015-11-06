package io.dropwizard.metrics.httpasyncclient;

import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.MetricRegistryListener;
import io.dropwizard.metrics.Timer;
import io.dropwizard.metrics.httpclient.HttpClientMetricNameStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstrumentedHttpClientsTest extends HttpClientTestBase {

    private final MetricRegistry metricRegistry = new MetricRegistry();

    private HttpAsyncClient asyncHttpClient;

    @Mock
    private HttpClientMetricNameStrategy metricNameStrategy;
    @Mock
    private MetricRegistryListener registryListener;

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        final HttpGet get = new HttpGet("/q=anything");
        final MetricName metricName = MetricName.build("some.made.up.metric.name");

        when(metricNameStrategy.getNameFor(anyString(), any(HttpRequest.class)))
                .thenReturn(metricName);

        asyncHttpClient.execute(host, get, null).get();

        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }

    @Before
    public void setUp() throws Exception {
        CloseableHttpAsyncClient chac = new InstrumentedNHttpClientBuilder(metricRegistry, metricNameStrategy).build();
        chac.start();
        asyncHttpClient = chac;
        metricRegistry.addListener(registryListener);
    }
}
