package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Gauge;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GaugeTest {
    final Gauge<String> gauge = new Gauge<String>() {
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
