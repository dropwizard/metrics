package com.codahale.metrics.httpclient5;

import com.codahale.metrics.MetricRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class InstrumentedHttpClientConnectionManagerTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();

    @Test
    public void shouldRemoveGauges() {
        final InstrumentedHttpClientConnectionManager instrumentedHttpClientConnectionManager = InstrumentedHttpClientConnectionManager.builder(metricRegistry).build();
        Assertions.assertEquals(4, metricRegistry.getGauges().size());

        instrumentedHttpClientConnectionManager.close();
        Assertions.assertEquals(0, metricRegistry.getGauges().size());

        // should be able to create another one with the same name ("")
        InstrumentedHttpClientConnectionManager.builder(metricRegistry).build().close();
    }

    @Test
    public void configurableViaBuilder() {
        final MetricRegistry registry = Mockito.mock(MetricRegistry.class);

        InstrumentedHttpClientConnectionManager.builder(registry)
                .name("some-name")
                .name("some-other-name")
                .build()
                .close();

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(registry, Mockito.atLeast(1)).register(argumentCaptor.capture(), any());
        assertTrue(argumentCaptor.getValue().contains("some-other-name"));
    }
}
