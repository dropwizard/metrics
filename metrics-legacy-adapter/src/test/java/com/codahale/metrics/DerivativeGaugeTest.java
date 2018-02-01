package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class DerivativeGaugeTest {

    @Test
    public void testCalculate() {
        DerivativeGauge<String, Integer> derivativeGauge = new DerivativeGauge<String, Integer>(() -> "23") {
            @Override
            protected Integer transform(String value) {
                return Integer.parseInt(value);
            }
        };
        assertThat(derivativeGauge.getValue()).isEqualTo(23);
    }
}
