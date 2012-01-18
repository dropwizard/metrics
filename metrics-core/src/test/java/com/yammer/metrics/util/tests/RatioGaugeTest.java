package com.yammer.metrics.util.tests;

import com.yammer.metrics.util.RatioGauge;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class RatioGaugeTest {
    @Test
    public void calculatesTheRatioOfTheNumeratorToTheDenominator() throws Exception {
        final RatioGauge regular = new RatioGauge() {
            @Override
            protected double getNumerator() {
                return 2;
            }

            @Override
            protected double getDenominator() {
                return 4;
            }
        };

        assertThat(regular.value(),
                   is(0.5));
    }

    @Test
    public void handlesDivideByZeroIssues() throws Exception {
        final RatioGauge divByZero = new RatioGauge() {
            @Override
            protected double getNumerator() {
                return 100;
            }

            @Override
            protected double getDenominator() {
                return 0;
            }
        };

        assertThat(divByZero.value(),
                   is(Double.NaN));
    }

    @Test
    public void handlesInfiniteDenominators() throws Exception {
        final RatioGauge infinite = new RatioGauge() {
            @Override
            protected double getNumerator() {
                return 10;
            }

            @Override
            protected double getDenominator() {
                return Double.POSITIVE_INFINITY;
            }
        };
        
        assertThat(infinite.value(),
                   is(Double.NaN));
    }

    @Test
    public void handlesNaNDenominators() throws Exception {
        final RatioGauge nan = new RatioGauge() {
            @Override
            protected double getNumerator() {
                return 10;
            }

            @Override
            protected double getDenominator() {
                return Double.NaN;
            }
        };
        
        assertThat(nan.value(),
                   is(Double.NaN));
    }
}
