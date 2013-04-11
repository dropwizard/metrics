package com.yammer.metrics.httpclient;

import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.MetricRegistryListener;
import com.yammer.metrics.Timer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstrumentedHttpClientTest {

    @Mock
    public HttpClientMetricNameStrategy metricNameStrategy;
    @Mock
    public MetricRegistryListener registryListener;

    private MetricRegistry metricRegistry;
    private HttpClient client;

    @Before
    public void init() {
        metricRegistry = new MetricRegistry();
        metricRegistry.addListener(registryListener);
        client = new InstrumentedHttpClient(metricRegistry, metricNameStrategy);
    }

    @Test
    public void registersExpectedMetricsGivenNameStrategy() throws Exception {
        HttpGet get = new HttpGet("http://google.com?q=anything");
        String metricName = "some.made.up.metric.name";

        when(metricNameStrategy.getNameFor(anyString(), eq(get))).thenReturn(metricName);

        try {
            client.execute(get);
        } catch (Exception e) {
        }

        verify(registryListener).onTimerAdded(eq(metricName), any(Timer.class));
    }
}
