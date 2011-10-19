package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.GaugeMetric;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GaugeMetricTest {
    final GaugeMetric<String> gauge = new GaugeMetric<String>() {
        @Override
        public String value() {
            return "woo";
        }
    };

    @Test
    public void returnsAValue() throws Exception {
        assertThat("a gauge returns a value",
                   gauge.value(),
                   is("woo"));
    }
}
