package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DerivativeGaugeTest {
    private final Gauge<String> gauge1 = new Gauge<String>() {
        @Override
        public String getValue() {
            return "woo";
        }
    };
    private final Gauge<Integer> gauge2 = new DerivativeGauge<String, Integer>(gauge1) {
        @Override
        protected Integer transform(String value) {
            return value.length();
        }
    };

    @Test
    public void returnsATransformedValue() throws Exception {
        assertThat(gauge2.getValue())
                .isEqualTo(3);
    }
}
