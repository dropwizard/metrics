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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore("The tests are flaky")
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

}
