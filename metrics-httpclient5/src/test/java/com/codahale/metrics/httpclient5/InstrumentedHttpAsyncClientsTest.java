package com.codahale.metrics.httpclient5;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstrumentedHttpAsyncClientsTest {
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private HttpClientMetricNameStrategy metricNameStrategy;
    @Mock
    private MetricRegistryListener registryListener;
    private HttpServer httpServer;
    private MetricRegistry metricRegistry;
    private CloseableHttpAsyncClient client;

    @Before
    public void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);

        metricRegistry = new MetricRegistry();
        metricRegistry.addListener(registryListener);
    }

    @After
    public void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        client = InstrumentedHttpAsyncClients.custom(metricRegistry, metricNameStrategy).disableAutomaticRetries().build();
        client.start();

        final SimpleHttpRequest request = SimpleHttpRequests.get("http://localhost:" + httpServer.getAddress().getPort() + "/");
        final String metricName = "some.made.up.metric.name";

        httpServer.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, 0L);
            exchange.setStreams(null, null);
            exchange.getResponseBody().write("TEST".getBytes(StandardCharsets.US_ASCII));
            exchange.close();
        });
        httpServer.start();

        when(metricNameStrategy.getNameFor(any(), any(HttpRequest.class))).thenReturn(metricName);

        final Future<SimpleHttpResponse> responseFuture = client.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse result) {
                assertThat(result.getBodyText()).isEqualTo("TEST");
            }

            @Override
            public void failed(Exception ex) {
                fail();
            }

            @Override
            public void cancelled() {
                fail();
            }
        });
        responseFuture.get(1L, TimeUnit.SECONDS);

        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }

    @Test
    public void registersExpectedExceptionMetrics() throws Exception {
        client = InstrumentedHttpAsyncClients.custom(metricRegistry, metricNameStrategy).disableAutomaticRetries().build();
        client.start();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final SimpleHttpRequest request = SimpleHttpRequests.get("http://localhost:" + httpServer.getAddress().getPort() + "/");
        final String requestMetricName = "request";
        final String exceptionMetricName = "exception";

        httpServer.createContext("/", HttpExchange::close);
        httpServer.start();

        when(metricNameStrategy.getNameFor(any(), any(HttpRequest.class)))
                .thenReturn(requestMetricName);
        when(metricNameStrategy.getNameFor(any(), any(Exception.class)))
                .thenReturn(exceptionMetricName);

        try {
            final Future<SimpleHttpResponse> responseFuture = client.execute(request, new FutureCallback<SimpleHttpResponse>() {
                @Override
                public void completed(SimpleHttpResponse result) {
                    fail();
                }

                @Override
                public void failed(Exception ex) {
                    countDownLatch.countDown();
                }

                @Override
                public void cancelled() {
                    fail();
                }
            });
            countDownLatch.await(5, TimeUnit.SECONDS);
            responseFuture.get(5, TimeUnit.SECONDS);

            fail();
        } catch (ExecutionException e) {
            assertThat(e).hasCauseInstanceOf(ConnectionClosedException.class);
            await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(metricRegistry.getMeters()).containsKey("exception"));
        }
    }

    @Test
    public void usesCustomClientConnectionManager() throws Exception {
        try(PoolingAsyncClientConnectionManager clientConnectionManager = spy(new PoolingAsyncClientConnectionManager())) {
        client = InstrumentedHttpAsyncClients.custom(metricRegistry, metricNameStrategy, clientConnectionManager).disableAutomaticRetries().build();
        client.start();

        final SimpleHttpRequest request = SimpleHttpRequests.get("http://localhost:" + httpServer.getAddress().getPort() + "/");
        final String metricName = "some.made.up.metric.name";

        httpServer.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, 0L);
            exchange.setStreams(null, null);
            exchange.getResponseBody().write("TEST".getBytes(StandardCharsets.US_ASCII));
            exchange.close();
        });
        httpServer.start();

        when(metricNameStrategy.getNameFor(any(), any(HttpRequest.class))).thenReturn(metricName);

        final Future<SimpleHttpResponse> responseFuture = client.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse result) {
                assertThat(result.getCode()).isEqualTo(200);
            }

            @Override
            public void failed(Exception ex) {
                fail();
            }

            @Override
            public void cancelled() {
                fail();
            }
        });
        responseFuture.get(1L, TimeUnit.SECONDS);

        verify(clientConnectionManager, atLeastOnce()).connect(any(), any(), any(), any(), any(), any());
        }
    }
}
