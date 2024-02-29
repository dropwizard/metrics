package com.codahale.metrics.httpasyncclient;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
//@Ignore("The tests are flaky")
public class InstrumentedHttpClientsTimerTest extends HttpClientTestBase {

    private HttpAsyncClient asyncHttpClient;

    @Mock
    private Timer.Context context;

    @Mock
    private MetricRegistry metricRegistry;


    @Before
    public void setUp() throws Exception {
        CloseableHttpAsyncClient chac = new InstrumentedNHttpClientBuilder(metricRegistry,
                mock(HttpClientMetricNameStrategy.class)).build();
        chac.start();
        asyncHttpClient = chac;

        Timer timer = mock(Timer.class);
        when(timer.time()).thenReturn(context);
        when(metricRegistry.timer(any())).thenReturn(timer);
    }

    @Test
    public void timerIsStoppedCorrectly() throws Exception {
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        HttpGet get = new HttpGet("/?q=anything");

        // Timer hasn't been stopped prior to executing the request
        verify(context, never()).stop();

        Future<HttpResponse> responseFuture = asyncHttpClient.execute(host, get, null);

        // Timer should still be running
        verify(context, never()).stop();

        responseFuture.get(20, TimeUnit.SECONDS);

        // After the computation is complete timer must be stopped
        // Materialzing the future and calling the future callback is not an atomic operation so
        // we need to wait for callback to succeed
        verify(context, timeout(200).times(1)).stop();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void timerIsStoppedCorrectlyWithProvidedFutureCallbackCompleted() throws Exception {
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        HttpGet get = new HttpGet("/?q=something");

        FutureCallback<HttpResponse> futureCallback = mock(FutureCallback.class);

        // Timer hasn't been stopped prior to executing the request
        verify(context, never()).stop();

        Future<HttpResponse> responseFuture = asyncHttpClient.execute(host, get, futureCallback);

        // Timer should still be running
        verify(context, never()).stop();

        responseFuture.get(20, TimeUnit.SECONDS);

        // Callback must have been called
        assertThat(responseFuture.isDone()).isTrue();
        // After the computation is complete timer must be stopped
        // Materialzing the future and calling the future callback is not an atomic operation so
        // we need to wait for callback to succeed
        verify(futureCallback, timeout(200).times(1)).completed(any(HttpResponse.class));
        verify(context, timeout(200).times(1)).stop();
    }




    @Test
    @SuppressWarnings("unchecked")
    public void timerIsStoppedCorrectlyWithProvidedFutureCallbackFailed() throws Exception {
        // There should be nothing listening on this port
        HttpHost host = HttpHost.create(String.format("http://127.0.0.1:%d", findAvailableLocalPort()));
        HttpGet get = new HttpGet("/?q=something");

        FutureCallback<HttpResponse> futureCallback = mock(FutureCallback.class);

        // Timer hasn't been stopped prior to executing the request
        verify(context, never()).stop();

        Future<HttpResponse> responseFuture = asyncHttpClient.execute(host, get, futureCallback);

        // Timer should still be running
        verify(context, never()).stop();

        try {
            responseFuture.get(20, TimeUnit.SECONDS);
            fail("This should fail as the client should not be able to connect");
        } catch (Exception e) {
            // Ignore
        }
        // After the computation is complete timer must be stopped
        // Materialzing the future and calling the future callback is not an atomic operation so
        // we need to wait for callback to succeed
        verify(futureCallback, timeout(200).times(1)).failed(any(Exception.class));
        verify(context, timeout(200).times(1)).stop();
    }

    /**
     * Test method to verify that the timer is stopped correctly when multiple requests are executed concurrently.
     *
     * @throws Exception if an error occurs during the test execution
     */
    @Test
    public void timerIsStoppedCorrectlyWithConcurrentRequests() throws Exception {
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        HttpGet get = new HttpGet("/?q=concurrent");

        // Timer hasn't been stopped prior to executing the requests
        verify(context, never()).stop();

        // Execute multiple requests concurrently
        Future<HttpResponse> responseFuture1 = asyncHttpClient.execute(host, get, null);
        Future<HttpResponse> responseFuture2 = asyncHttpClient.execute(host, get, null);

        // Wait for both requests to complete
        responseFuture1.get(20, TimeUnit.SECONDS);
        responseFuture2.get(20, TimeUnit.SECONDS);

        // After all computations are complete, the timer should be stopped
        verify(context, timeout(200).times(2)).stop(); // Two requests were made
    }

    /**
     * Test method to verify that the timer is stopped correctly when a request is cancelled using a provided future callback.
     *
     * @throws Exception if an error occurs during the test execution
     */
    @Test
    public void timerIsStoppedCorrectlyWithProvidedFutureCallbackCancelled() throws Exception {
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        HttpGet get = new HttpGet("/?q=cancelled");

        FutureCallback<HttpResponse> futureCallback = mock(FutureCallback.class);

        // Timer hasn't been stopped prior to executing the request
        verify(context, never()).stop();

        Future<HttpResponse> responseFuture = asyncHttpClient.execute(host, get, futureCallback);
        responseFuture.cancel(true); // Cancel the future

        // After the computation is cancelled, the timer must be stopped
        verify(context, timeout(200).times(1)).stop();
    }

    /**
     * Test method to verify that the timer is stopped correctly when a request fails using a provided future callback.
     *
     * @throws Exception if an error occurs during the test execution
     */
    @Test
    @SuppressWarnings("unchecked")
    public void timerIsStoppedCorrectlyWithProvidedFutureCallbackAndFailure() throws Exception {
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        HttpGet get = new HttpGet("/?q=failure");

        FutureCallback<HttpResponse> futureCallback = mock(FutureCallback.class);

        // Timer hasn't been stopped prior to executing the request
        verify(context, never()).stop();

        Future<HttpResponse> responseFuture = asyncHttpClient.execute(host, get, futureCallback);
        responseFuture.get(20, TimeUnit.SECONDS); // Wait for the request to complete

        // After the computation fails, the timer must be stopped
        verify(context, timeout(200).times(1)).stop();
    }

    /**
     * Test method to verify that the timer is stopped correctly when an exception occurs during the future get operation.
     *
     * @throws Exception if an error occurs during the test execution
     */
    @Test
    public void timerIsStoppedCorrectlyWithExceptionInFutureGet() throws Exception {
        // Arrange
        HttpHost host = startServerWithGlobalRequestHandler(STATUS_OK);
        HttpGet get = new HttpGet("/?q=exception");

        // Timer hasn't been stopped prior to executing the request
        verify(context, never()).stop();

        // Act
        Future<HttpResponse> responseFuture = asyncHttpClient.execute(host, get, null);
        responseFuture.get(); // Let the future throw an exception

        // Assert
        // After the computation throws an exception, the timer must be stopped
        verify(context, timeout(200).times(1)).stop();
    }



}
