package com.codahale.metrics.httpclient;

import com.codahale.metrics.MetricRegistry;
import org.apache.http.client.HttpClient;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class InstrumentedHttpClientTest {
    private final MetricRegistry registry = new MetricRegistry();
    private final HttpClient client = new InstrumentedHttpClient(registry);

    @Test
    public void hasAnInstrumentedConnectionManager() throws Exception {

        assertThat(client.getConnectionManager())
                .isInstanceOf(InstrumentedClientConnManager.class);
    }
}
