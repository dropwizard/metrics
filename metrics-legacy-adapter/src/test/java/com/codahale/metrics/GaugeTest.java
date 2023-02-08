package com.codahale.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class GaugeTest {

    private Gauge<Integer> gauge = () -> 83;

    @Test
    void testGetValue() {
        assertThat(gauge.getValue()).isEqualTo(83);
    }
}
