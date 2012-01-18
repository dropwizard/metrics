package com.yammer.metrics.util;

import com.yammer.metrics.core.Gauge;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;

/**
 * A gauge which measures the ratio of one value to another.
 * <p/>
 * If the denominator is zero, not a number, or infinite, the resulting ratio is not a number.
 */
public abstract class RatioGauge extends Gauge<Double> {
    /**
     * Returns the numerator (the value on the top half of the fraction or the left-hand side of the
     * ratio).
     *
     * @return the numerator
     */
    protected abstract double getNumerator();

    /**
     * Returns the denominator (the value on the bottom half of the fraction or the right-hand side
     * of the ratio).
     *
     * @return the denominator
     */
    protected abstract double getDenominator();

    @Override
    public Double value() {
        final double d = getDenominator();
        if (isNaN(d) || isInfinite(d) || d == 0.0) {
            return Double.NaN;
        }
        return getNumerator() / d;
    }
}
