package com.yammer.metrics.util.tests;

import com.yammer.metrics.util.PercentGauge;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PercentGaugeTest {
    @Test
    public void returnsAPercentage() throws Exception {
        final PercentGauge gauge = new PercentGauge() {
            @Override
            protected double getNumerator() {
                return 2;
            }

            @Override
            protected double getDenominator() {
                return 4;
            }
        };

        assertThat(gauge.value(),
                   is(50.0));
    }

    @Test
    public void handlesNaN() throws Exception {
        final PercentGauge gauge = new PercentGauge() {
            @Override
            protected double getNumerator() {
                return 2;
            }

            @Override
            protected double getDenominator() {
                return 0;
            }
        };
        
        assertThat(gauge.value(),
                   is(Double.NaN));
    }
}
