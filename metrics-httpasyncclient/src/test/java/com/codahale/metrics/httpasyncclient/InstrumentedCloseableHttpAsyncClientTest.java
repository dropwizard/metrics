package com.codahale.metrics.httpasyncclient;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpRequestMetricNameStrategies;
import com.codahale.metrics.httpclient.HttpRequestMetricNameStrategy;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


/**
 *
 */
public class InstrumentedCloseableHttpAsyncClientTest {

    private MetricRegistry metricRegistry;
    private HttpRequestMetricNameStrategy metricNameStrategy;
    private String name;

    @Mock
    private CloseableHttpAsyncClient closeableHttpAsyncClient;

    private CloseableHttpAsyncClient instrumented;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        metricRegistry = new MetricRegistry();
        metricNameStrategy = HttpRequestMetricNameStrategies.METHOD_ONLY;
        name = "test";
        instrumented = new InstrumentedCloseableHttpAsyncClient(closeableHttpAsyncClient, metricRegistry, metricNameStrategy, name);
    }

    @Test
    public void testExecute() throws Exception {
        HttpAsyncRequestProducer requestProducer = mock(HttpAsyncRequestProducer.class);
        HttpAsyncResponseConsumer responseConsumer = mock(HttpAsyncResponseConsumer.class);
        HttpContext context = mock(HttpContext.class);
        FutureCallback responseCallback = mock(FutureCallback.class);
        HttpRequest httpRequest = new HttpPost();
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(requestProducer.generateRequest()).thenReturn(httpRequest);

        instrumented.execute(requestProducer, responseConsumer, context, responseCallback);

        ArgumentCaptor<HttpAsyncRequestProducer> captureProducer = ArgumentCaptor.forClass(HttpAsyncRequestProducer.class);
        ArgumentCaptor<HttpAsyncResponseConsumer> captureConsumer = ArgumentCaptor.forClass(HttpAsyncResponseConsumer.class);
        verify(closeableHttpAsyncClient).execute(captureProducer.capture(), captureConsumer.capture(), eq(context), eq(responseCallback));

        Counter active = metricRegistry.counter(metricNameStrategy.getNameForActive(HttpAsyncClient.class, name, httpRequest));
//        Timer duration = metricRegistry.timer(metricNameStrategy.getNameForDuration(HttpAsyncClient.class, name, httpRequest));

        HttpAsyncRequestProducer producer = captureProducer.getValue();
        assertThat(active.getCount()).isEqualTo(0);
        producer.generateRequest();
        assertThat(active.getCount()).isEqualTo(1);
        producer.failed(null);
        assertThat(active.getCount()).isEqualTo(0);

        producer.generateRequest();
        assertThat(active.getCount()).isEqualTo(1);
        producer.requestCompleted(context);
        assertThat(active.getCount()).isEqualTo(0);

        HttpAsyncResponseConsumer consumer = captureConsumer.getValue();
        assertThat(active.getCount()).isEqualTo(0);
        consumer.responseReceived(httpResponse);
        assertThat(active.getCount()).isEqualTo(1);
        consumer.responseCompleted(context);
        assertThat(active.getCount()).isEqualTo(0);
    }
}
