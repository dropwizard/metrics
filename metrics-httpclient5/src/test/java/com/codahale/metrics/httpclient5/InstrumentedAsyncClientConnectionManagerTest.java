package com.codahale.metrics.httpclient5;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class InstrumentedAsyncClientConnectionManagerTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();

    @Test
    public void shouldRemoveGauges() {
        final InstrumentedAsyncClientConnectionManager instrumentedHttpClientConnectionManager = InstrumentedAsyncClientConnectionManager.builder(metricRegistry).build();
        Assert.assertEquals(4, metricRegistry.getGauges().size());

        instrumentedHttpClientConnectionManager.close();
        Assert.assertEquals(0, metricRegistry.getGauges().size());

        // should be able to create another one with the same name ("")
        InstrumentedHttpClientConnectionManager.builder(metricRegistry).build().close();
    }

    @Test
    public void configurableViaBuilder() {
        final MetricRegistry registry = Mockito.mock(MetricRegistry.class);

        InstrumentedAsyncClientConnectionManager.builder(registry)
                .name("some-name")
                .name("some-other-name")
                .build()
                .close();

        ArgumentCaptor<MetricName> argumentCaptor = ArgumentCaptor.forClass(MetricName.class);
        Mockito.verify(registry, Mockito.atLeast(1)).register(argumentCaptor.capture(), any());
        assertTrue(argumentCaptor.getValue().getKey().contains("some-other-name"));
    }
}
