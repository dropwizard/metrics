package io.dropwizard.metrics5.httpclient;

import io.dropwizard.metrics5.MetricRegistry;
import org.junit.Assert;
import org.junit.Test;


public class InstrumentedHttpClientConnectionManagerTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();

    @Test
    public void shouldRemoveGauges() {
        final InstrumentedHttpClientConnectionManager instrumentedHttpClientConnectionManager = new InstrumentedHttpClientConnectionManager(metricRegistry);
        Assert.assertEquals(4, metricRegistry.getGauges().size());

        instrumentedHttpClientConnectionManager.close();
        Assert.assertEquals(0, metricRegistry.getGauges().size());

        // should be able to create another one with the same name ("")
        try (InstrumentedHttpClientConnectionManager unused = new InstrumentedHttpClientConnectionManager(metricRegistry)) {
        }
    }
}
