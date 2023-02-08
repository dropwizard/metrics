package io.dropwizard.metrics5.httpclient5;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

class InstrumentedHttpClientConnectionManagerTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();

    @Test
    void shouldRemoveGauges() {
        final InstrumentedHttpClientConnectionManager instrumentedHttpClientConnectionManager = InstrumentedHttpClientConnectionManager.builder(metricRegistry).build();
        assertThat(metricRegistry.getGauges().entrySet().stream()
                .map(e -> entry(e.getKey().getKey(), (Integer) e.getValue().getValue())))
                .containsOnly(entry("org.apache.hc.client5.http.io.HttpClientConnectionManager.available-connections", 0),
                        entry("org.apache.hc.client5.http.io.HttpClientConnectionManager.leased-connections", 0),
                        entry("org.apache.hc.client5.http.io.HttpClientConnectionManager.max-connections", 25),
                        entry("org.apache.hc.client5.http.io.HttpClientConnectionManager.pending-connections", 0));

        instrumentedHttpClientConnectionManager.close();
        assertEquals(0, metricRegistry.getGauges().size());

        // should be able to create another one with the same name ("")
        InstrumentedHttpClientConnectionManager.builder(metricRegistry).build().close();
    }

    @Test
    void configurableViaBuilder() {
        final MetricRegistry registry = Mockito.mock(MetricRegistry.class);

        InstrumentedHttpClientConnectionManager.builder(registry)
                .name("some-name")
                .name("some-other-name")
                .build()
                .close();

        ArgumentCaptor<MetricName> argumentCaptor = ArgumentCaptor.forClass(MetricName.class);
        Mockito.verify(registry, Mockito.atLeast(1)).registerGauge(argumentCaptor.capture(), any());
        assertTrue(argumentCaptor.getValue().getKey().contains("some-other-name"));
    }
}
