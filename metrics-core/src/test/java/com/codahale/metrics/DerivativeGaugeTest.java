package com.codahale.metrics;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DerivativeGaugeTest {

    private Gauge<String> gauge1 = mock(Gauge.class);
    private final Gauge<Integer> gauge2 = new DerivativeGauge<String, Integer>(gauge1) {
        @Override
        protected Integer transform(String value) {
            return value.length();
        }
    };

    @Before
    public void setup() {
        when(gauge1.getValue()).thenReturn("woo");
    }

    @Test
    public void returnsATransformedValue() throws Exception {
        assertThat(gauge2.getValue())
                .isEqualTo(3);
    }
}
