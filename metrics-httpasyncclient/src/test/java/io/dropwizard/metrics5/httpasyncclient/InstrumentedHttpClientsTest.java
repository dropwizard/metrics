package io.dropwizard.metrics5.httpasyncclient;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.MetricRegistryListener;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.httpclient.HttpClientMetricNameStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class InstrumentedHttpClientsTest extends HttpClientTestBase {

    private final MetricRegistry metricRegistry = new MetricRegistry();


    private HttpAsyncClient asyncHttpClient;
    @Mock
    private HttpClientMetricNameStrategy metricNameStrategy;
    @Mock
    private MetricRegistryListener registryListener;

    @Test
    void registersExpectedMetricsGivenNameStrategy() throws Exception {
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        final HttpGet get = new HttpGet("/q=anything");
        final MetricName metricName = MetricName.build("some.made.up.metric.name");

        when(metricNameStrategy.getNameFor(any(), any(HttpRequest.class)))
        .thenReturn(metricName);

        asyncHttpClient.execute(host, get, null).get();

        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }

    @BeforeEach
    void setUp() throws Exception {
        CloseableHttpAsyncClient chac = new InstrumentedNHttpClientBuilder(metricRegistry, metricNameStrategy).build();
        chac.start();
        asyncHttpClient = chac;
        metricRegistry.addListener(registryListener);
    }
}
