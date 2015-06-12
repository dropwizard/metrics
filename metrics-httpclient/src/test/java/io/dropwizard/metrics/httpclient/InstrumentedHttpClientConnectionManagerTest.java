package io.dropwizard.metrics.httpclient;

import org.junit.Assert;
import org.junit.Test;

import io.dropwizard.metrics.httpclient.InstrumentedHttpClientConnectionManager;

import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.Timer;


public class InstrumentedHttpClientConnectionManagerTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();

    @Test
    public void shouldRemoveGauges() {
       final InstrumentedHttpClientConnectionManager instrumentedHttpClientConnectionManager = new InstrumentedHttpClientConnectionManager(metricRegistry);
        Assert.assertEquals(4, metricRegistry.getGauges().size());

        instrumentedHttpClientConnectionManager.close();
        Assert.assertEquals(0, metricRegistry.getGauges().size());

        // should be able to create another one with the same name ("")
        new InstrumentedHttpClientConnectionManager(metricRegistry);
    }
}
