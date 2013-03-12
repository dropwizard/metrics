package com.yammer.metrics;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;

/**
 * A gauge which measures the ratio of one value to another.
 * <p/>
 * If the denominator is zero, not a number, or infinite, the resulting ratio is not a number.
 */
public abstract class RatioGauge implements Gauge<Double> {
    public static class Ratio {
        public static Ratio of(double numerator, double denominator) {
            return new Ratio(numerator, denominator);
        }

        private final double numerator;
        private final double denominator;

        private Ratio(double numerator, double denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        public double getValue() {
            final double d = denominator;
            if (isNaN(d) || isInfinite(d) || d == 0) {
                return Double.NaN;
            }
            return numerator / d;
        }
    }

    /**
     * Returns the {@link Ratio} which is the gauge's current value.
     *
     * @return the {@link Ratio} which is the gauge's current value
     */
    protected abstract Ratio getRatio();

    @Override
    public Double getValue() {
        return getRatio().getValue();
    }
}
