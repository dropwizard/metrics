package com.yammer.metrics.httpclient.tests;

import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import com.yammer.metrics.httpclient.InstrumentedHttpClient;
import org.apache.http.client.HttpClient;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class InstrumentedHttpClientTest {
    private final MetricRegistry registry = new MetricRegistry("test");
    private final HttpClient client = new InstrumentedHttpClient(registry);

    @Test
    public void hasAnInstrumentedConnectionManager() throws Exception {

        assertThat(client.getConnectionManager())
                .isInstanceOf(InstrumentedClientConnManager.class);
    }
}
