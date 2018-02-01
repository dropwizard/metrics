package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class GaugeTest {

    private Gauge<Integer> gauge = () -> 83;

    @Test
    public void testGetValue() {
        assertThat(gauge.getValue()).isEqualTo(83);
    }
}
