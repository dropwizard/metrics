package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultSettableGaugeTest {
    @Test
    public void newSettableGaugeWithoutDefaultReturnsNull() {
        DefaultSettableGauge<String> gauge = new DefaultSettableGauge<>();
        assertThat(gauge.getValue()).isNull();
    }

    @Test
    public void newSettableGaugeWithDefaultReturnsDefault() {
        DefaultSettableGauge<String> gauge = new DefaultSettableGauge<>("default");
        assertThat(gauge.getValue()).isEqualTo("default");
    }

    @Test
    public void setValueOverwritesExistingValue() {
        DefaultSettableGauge<String> gauge = new DefaultSettableGauge<>("default");
        gauge.setValue("test");
        assertThat(gauge.getValue()).isEqualTo("test");
    }
}
