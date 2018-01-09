package io.dropwizard.metrics5.httpclient;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.MetricRegistryListener;
import io.dropwizard.metrics5.Timer;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstrumentedHttpClientsTest {
    private final HttpClientMetricNameStrategy metricNameStrategy =
            mock(HttpClientMetricNameStrategy.class);
    private final MetricRegistryListener registryListener =
            mock(MetricRegistryListener.class);
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final HttpClient client =
            InstrumentedHttpClients.custom(metricRegistry, metricNameStrategy).disableAutomaticRetries().build();

    @Before
    public void setUp() {
        metricRegistry.addListener(registryListener);
    }

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        final HttpGet get = new HttpGet("http://example.com?q=anything");
        final MetricName metricName = MetricName.build("some.made.up.metric.name");

        when(metricNameStrategy.getNameFor(any(), any(HttpRequest.class)))
                .thenReturn(metricName);

        client.execute(get);

        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }

    @Test
    public void registersExpectedExceptionMetrics() throws Exception {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);

        final HttpGet get = new HttpGet("http://localhost:" + httpServer.getAddress().getPort() + "/");
        final MetricName requestMetricName = MetricName.build("request");
        final MetricName exceptionMetricName = MetricName.build("exception");

        httpServer.createContext("/", HttpExchange::close);
        httpServer.start();

        when(metricNameStrategy.getNameFor(any(), any(HttpRequest.class)))
                .thenReturn(requestMetricName);
        when(metricNameStrategy.getNameFor(any(), any(Exception.class)))
                .thenReturn(exceptionMetricName);

        try {
            client.execute(get);
            fail();
        } catch (NoHttpResponseException expected) {
            assertThat(metricRegistry.getMeters()).containsKey(MetricName.build("exception"));
        } finally {
            httpServer.stop(0);
        }
    }
}
