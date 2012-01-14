package com.yammer.metrics.httpclient.tests;

import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import com.yammer.metrics.httpclient.InstrumentedHttpClient;
import org.apache.http.client.HttpClient;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedHttpClientTest {
    private final HttpClient client = new InstrumentedHttpClient();

    @Test
    public void hasAnInstrumentedConnectionManager() throws Exception {

        assertThat(client.getConnectionManager(),
                   is(instanceOf(InstrumentedClientConnManager.class)));
    }
}
