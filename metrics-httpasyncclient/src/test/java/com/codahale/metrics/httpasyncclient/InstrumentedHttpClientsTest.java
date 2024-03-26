package com.codahale.metrics.httpasyncclient;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import com.codahale.metrics.httpclient.InstrumentedHttpClients;
import org.apache.http.HttpRequest;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test class for {@link InstrumentedHttpClients}.
 * This class tests the registration of expected metrics using various configurations of metric registries,
 * metric name strategies, and HTTP clients.
 */
@RunWith(MockitoJUnitRunner.class)
public class InstrumentedHttpClientsTest extends HttpClientTestBase {

    // Metric registry for testing
    private final MetricRegistry metricRegistry = new MetricRegistry();

    // HTTP client instance
    private HttpAsyncClient asyncHttpClient;

    // Mocked objects
    @Mock
    private HttpClientMetricNameStrategy metricNameStrategy;
    @Mock
    private MetricRegistryListener registryListener;

    /**
     * Test method to verify the registration of expected metrics using a given name strategy.
     *
     * @throws Exception if an error occurs during the test execution
     */
    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        // Arrange
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        final HttpGet get = new HttpGet("/q=anything");
        final String metricName = MetricRegistry.name("some.made.up.metric.name");

        // Stubbing behavior of the metric name strategy
        when(metricNameStrategy.getNameFor(any(), any(HttpRequest.class))).thenReturn(metricName);

        // Act
        asyncHttpClient.execute(host, get, null).get();

        // Assert
        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }

    /**
     * Test method to verify the creation of a closeable HTTP async client with a metric registry and name strategy.
     */
    @Test
    public void testBuildWithMetricRegistryAndNameStrategy() {
        // Arrange
        InstrumentedNHttpClientBuilder builder = new InstrumentedNHttpClientBuilder(metricRegistry, metricNameStrategy, "test-client");

        // Act
        CloseableHttpAsyncClient client = builder.build();

        // Assert
        assertNotNull(client);
    }

    /**
     * Test method to verify the registration of expected metrics using a name strategy with a built client.
     *
     * @throws Exception if an error occurs during the test execution
     */
    @Ignore // Ignored due to incomplete test setup
    @Test
    public void testRegistersExpectedMetricsGivenNameStrategy() throws Exception {
        // Arrange
        InstrumentedNHttpClientBuilder builder = new InstrumentedNHttpClientBuilder(metricRegistry, metricNameStrategy);
        CloseableHttpAsyncClient asyncHttpClient = builder.build();
        asyncHttpClient.start();

        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        HttpGet get = new HttpGet("/q=anything");
        asyncHttpClient.execute(host, get, null).get();

        ArgumentCaptor<String> metricNameCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        verify(metricNameStrategy).getNameFor(any(), any(HttpRequest.class));
        verify(registryListener).onTimerAdded(metricNameCaptor.capture(), any(Timer.class));
    }

    /**
     * Set up method executed before each test case.
     * Initializes the HTTP async client and adds a metric registry listener.
     *
     * @throws Exception if an error occurs during the setup
     */
    @Before
    public void setUp() throws Exception {
        // Initialize the HTTP async client
        CloseableHttpAsyncClient chac = new InstrumentedNHttpClientBuilder(metricRegistry, metricNameStrategy).build();
        chac.start();
        asyncHttpClient = chac;

        // Add metric registry listener
        metricRegistry.addListener(registryListener);
    }
}
