package com.yammer.metrics.tests;

import com.yammer.metrics.RatioGauge;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RatioGaugeTest {
    @Test
    public void calculatesTheRatioOfTheNumeratorToTheDenominator() throws Exception {
        final RatioGauge regular = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return RatioGauge.Ratio.of(2, 4);
            }
        };

        assertThat(regular.getValue(),
                   is(0.5));
    }

    @Test
    public void handlesDivideByZeroIssues() throws Exception {
        final RatioGauge divByZero = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(100, 0);
            }
        };

        assertThat(divByZero.getValue(),
                   is(Double.NaN));
    }

    @Test
    public void handlesInfiniteDenominators() throws Exception {
        final RatioGauge infinite = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(10, Double.POSITIVE_INFINITY);
            }
        };
        
        assertThat(infinite.getValue(),
                   is(Double.NaN));
    }

    @Test
    public void handlesNaNDenominators() throws Exception {
        final RatioGauge nan = new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(10, Double.NaN);
            }
        };
        
        assertThat(nan.getValue(),
                   is(Double.NaN));
    }
}
