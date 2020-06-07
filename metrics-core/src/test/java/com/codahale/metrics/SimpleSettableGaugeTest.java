package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleSettableGaugeTest {

    @Test
    public void defaultValue() {
        SimpleSettableGauge<Integer> settable = new SimpleSettableGauge<>(1);

        assertThat(settable.getValue()).isEqualTo(1);
    }

    @Test
    public void setValueAndThenGetValue() {
        SimpleSettableGauge<String> settable = new SimpleSettableGauge<>("default");

        settable.setValue("first");
        assertThat(settable.getValue())
                .isEqualTo("first");

        settable.setValue("second");
        assertThat(settable.getValue())
                .isEqualTo("second");
    }
}
