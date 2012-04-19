package com.yammer.metrics.util;

/**
 * A {@link RatioGauge} extension which returns a percentage, not a ratio.
 */
public abstract class PercentGauge extends RatioGauge {
    private static final int ONE_HUNDRED = 100;

    @Override
    public Double getValue() {
        return super.getValue() * ONE_HUNDRED;
    }
}
