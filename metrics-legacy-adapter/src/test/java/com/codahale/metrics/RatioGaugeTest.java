package com.codahale.metrics;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class RatioGaugeTest {

    private RatioGauge ratioGauge = new RatioGauge() {
        @Override
        protected Ratio getRatio() {
            return Ratio.of(1, 3);
        }
    };

    @Test
    void testViewRatin() {
        assertThat(ratioGauge.getRatio().toString()).isEqualTo("1.0:3.0");
    }


    @Test
    void testCalculateRatio() {
        assertThat(ratioGauge.getValue()).isCloseTo(0.33, Offset.offset(0.01));
    }
}
