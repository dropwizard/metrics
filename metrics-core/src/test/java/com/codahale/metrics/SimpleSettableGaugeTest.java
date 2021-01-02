package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleSettableGaugeTest {

    @Test
    public void defaultValue() {
        DefaultSettableGauge<Integer> settable = new DefaultSettableGauge<>(1);

        assertThat(settable.getValue()).isEqualTo(1);
    }

    @Test
    public void setValueAndThenGetValue() {
        DefaultSettableGauge<String> settable = new DefaultSettableGauge<>("default");

        settable.setValue("first");
        assertThat(settable.getValue())
                .isEqualTo("first");

        settable.setValue("second");
        assertThat(settable.getValue())
                .isEqualTo("second");
    }
}
