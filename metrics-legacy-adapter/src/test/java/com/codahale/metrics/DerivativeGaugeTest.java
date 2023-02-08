package com.codahale.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class DerivativeGaugeTest {

    @Test
    void testCalculate() {
        DerivativeGauge<String, Integer> derivativeGauge = new DerivativeGauge<String, Integer>(() -> "23") {
            @Override
            protected Integer transform(String value) {
                return Integer.parseInt(value);
            }
        };
        assertThat(derivativeGauge.getValue()).isEqualTo(23);
    }
}
