package io.dropwizard.metrics.httpasyncclient;

import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.Timer;
import io.dropwizard.metrics.httpclient.HttpClientMetricNameStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InstrumentedHttpClientsTimerTest {

    private HttpAsyncClient hac;

    @Mock
    private MetricRegistry metricRegistry;

    @Mock
    private Timer.Context context;

    @Before
    public void setUp() throws Exception {
        CloseableHttpAsyncClient chac = new InstrumentedNHttpClientBuilder(metricRegistry, mock(HttpClientMetricNameStrategy.class)).build();
        chac.start();
        hac = chac;

        Timer timer = mock(Timer.class);
        when(timer.time()).thenReturn(context);
        when(metricRegistry.timer(Matchers.<MetricName>anyObject())).thenReturn(timer);
    }

    @Test
    public void timerIsStoppedCorrectly() throws Exception {
        HttpGet get = new HttpGet("http://example.com?q=anything");

        // Timer hasn't been stopped prior to executing the request
        verify(context, never()).stop();

        Future<HttpResponse> responseFuture = hac.execute(get, null);

        // Timer should still be running
        verify(context, never()).stop();

        responseFuture.get(20, TimeUnit.SECONDS);

        // After the computation is complete timer must be stopped
        // Materialzing the future and calling the future callback is not an atomic operation so
        // we need to wait for callback to succeed
        verify(context, timeout(100).times(1)).stop();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void timerIsStoppedCorrectlyWithProvidedFutureCallback() throws Exception {
        HttpGet get = new HttpGet("http://example.com?q=something");

        FutureCallback<HttpResponse> futureCallback = mock(FutureCallback.class);

        // Timer hasn't been stopped prior to executing the request
        verify(context, never()).stop();

        Future<HttpResponse> responseFuture = hac.execute(get, futureCallback);

        // Timer should still be running
        verify(context, never()).stop();

        responseFuture.get(20, TimeUnit.SECONDS);

        // Callback must have been called
        assertTrue(responseFuture.isDone());
        // After the computation is complete timer must be stopped
        // Materialzing the future and calling the future callback is not an atomic operation so
        // we need to wait for callback to succeed
        verify(futureCallback, timeout(100).times(1)).completed(Matchers.<HttpResponse>anyObject());
        verify(context, timeout(100).times(1)).stop();
    }
}
