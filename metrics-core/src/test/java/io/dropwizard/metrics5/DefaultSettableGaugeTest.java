package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSettableGaugeTest {
    @Test
    void newSettableGaugeWithoutDefaultReturnsNull() {
        DefaultSettableGauge<String> gauge = new DefaultSettableGauge<>();
        assertThat(gauge.getValue()).isNull();
    }

    @Test
    void newSettableGaugeWithDefaultReturnsDefault() {
        DefaultSettableGauge<String> gauge = new DefaultSettableGauge<>("default");
        assertThat(gauge.getValue()).isEqualTo("default");
    }

    @Test
    void setValueOverwritesExistingValue() {
        DefaultSettableGauge<String> gauge = new DefaultSettableGauge<>("default");
        gauge.setValue("test");
        assertThat(gauge.getValue()).isEqualTo("test");
    }
}
