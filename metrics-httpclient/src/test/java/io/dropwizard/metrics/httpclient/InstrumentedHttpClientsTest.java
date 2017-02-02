package io.dropwizard.metrics.httpclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.MetricRegistryListener;
import io.dropwizard.metrics.Timer;
import static org.junit.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
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
    private final HttpClient client =
            InstrumentedHttpClients.custom(metricRegistry, metricNameStrategy).disableAutomaticRetries().build();

    @Before
    public void setUp() throws Exception {
        metricRegistry.addListener(registryListener);
    }

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        final HttpGet get = new HttpGet("http://example.com?q=anything");
        final MetricName metricName = MetricName.build("some.made.up.metric.name");

        when(metricNameStrategy.getNameFor(anyString(), any(HttpRequest.class)))
                .thenReturn(metricName);

        client.execute(get);

        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }

    @Test
    public void registersExpectedExceptionMetrics() throws Exception {
        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress("localhost", 0));
        final HttpGet get = new HttpGet("http://localhost:" + server.getLocalPort() + "/");
        final MetricName requestMetricName = MetricName.build("request");
        final MetricName exceptionMetricName = MetricName.build("exception");

        Thread serverThread = new Thread(() -> {
            try {
                final Socket socket = server.accept();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        when(metricNameStrategy.getNameFor(anyString(), any(HttpRequest.class)))
            .thenReturn(requestMetricName);
        when(metricNameStrategy.getNameFor(anyString(), any(Exception.class)))
            .thenReturn(exceptionMetricName);

        try {
            client.execute(get);
            fail();
        } catch (IOException expected) {
            assertThat(metricRegistry.getMeters()).containsKey(new MetricName("exception"));
        } finally {
            serverThread.interrupt();
            serverThread.join();
        }
    }
}
