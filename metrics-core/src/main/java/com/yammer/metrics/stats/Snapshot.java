package com.yammer.metrics.stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static java.lang.Math.floor;

/**
 * A statistical snapshot of a {@link Snapshot}.
 */
public class Snapshot {
    private static final double MEDIAN_Q = 0.5;
    private static final double P75_Q = 0.75;
    private static final double P95_Q = 0.95;
    private static final double P98_Q = 0.98;
    private static final double P99_Q = 0.99;
    private static final double P999_Q = 0.999;

    private final double[] values;

    /**
     * Create a new {@link Snapshot} with the given values.
     *
     * @param values    an unordered set of values in the sample
     */
    public Snapshot(Collection<Long> values) {
        final Object[] copy = values.toArray();
        this.values = new double[copy.length];
        for (int i = 0; i < copy.length; i++) {
            this.values[i] = (Long) copy[i];
        }
        Arrays.sort(this.values);
    }

    /**
     * Create a new {@link Snapshot} with the given values.
     *
     * @param values    an unordered set of values in the sample
     */
    public Snapshot(double[] values) {
        this.values = new double[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
        Arrays.sort(this.values);
    }

    /**
     * Returns the value at the given quantile.
     *
     * @param quantile    a given quantile, in {@code [0..1]}
     * @return the value in the distribution at {@code quantile}
     */
    public double getValue(double quantile) {
        if (quantile < 0.0 || quantile > 1.0) {
            throw new IllegalArgumentException(quantile + " is not in [0..1]");
        }

        if (values.length == 0) {
            return 0.0;
        }

        final double pos = quantile * (values.length + 1);

        if (pos < 1) {
            return values[0];
        }

        if (pos >= values.length) {
            return values[values.length - 1];
        }

        final double lower = values[(int) pos - 1];
        final double upper = values[(int) pos];
        return lower + (pos - floor(pos)) * (upper - lower);
    }

    /**
     * Returns the number of values in the snapshot.
     *
     * @return the number of values in the snapshot
     */
    public int size() {
        return values.length;
    }

    /**
     * Returns the median value in the distribution.
     *
     * @return the median value in the distribution
     */
    public double getMedian() {
        return getValue(MEDIAN_Q);
    }

    /**
     * Returns the value at the 75th percentile in the distribution.
     *
     * @return the value at the 75th percentile in the distribution
     */
    public double get75thPercentile() {
        return getValue(P75_Q);
    }

    /**
     * Returns the value at the 95th percentile in the distribution.
     *
     * @return the value at the 95th percentile in the distribution
     */
    public double get95thPercentile() {
        return getValue(P95_Q);
    }

    /**
     * Returns the value at the 98th percentile in the distribution.
     *
     * @return the value at the 98th percentile in the distribution
     */
    public double get98thPercentile() {
        return getValue(P98_Q);
    }

    /**
     * Returns the value at the 99th percentile in the distribution.
     *
     * @return the value at the 99th percentile in the distribution
     */
    public double get99thPercentile() {
        return getValue(P99_Q);
    }

    /**
     * Returns the value at the 99.9th percentile in the distribution.
     *
     * @return the value at the 99.9th percentile in the distribution
     */
    public double get999thPercentile() {
        return getValue(P999_Q);
    }

    /**
     * Returns the entire set of values in the snapshot.
     *
     * @return the entire set of values in the snapshot
     */
    public double[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    /**
     * Writes the values of the sample to the given file.
     *
     * @param output the file to which the values will be written
     * @throws IOException if there is an error writing the values
     */
    public void dump(File output) throws IOException {
        final PrintWriter writer = new PrintWriter(output);
        try {
            for (double value : values) {
                writer.printf("%f\n", value);
            }
        } finally {
            writer.close();
        }
    }
}
