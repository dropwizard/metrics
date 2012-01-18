package com.yammer.metrics.util;

/**
 * A {@link RatioGauge} extension which returns a percentage, not a ratio.
 */
public abstract class PercentGauge extends RatioGauge {
    private static final int ONE_HUNDRED = 100;

    @Override
    public Double value() {
        return super.value() * ONE_HUNDRED;
    }
}
