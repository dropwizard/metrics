package com.codahale.metrics;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

import static java.lang.Math.floor;

/**
 * A statistical snapshot of a {@link UniformSnapshot}.
 */
public class UniformSnapshot extends Snapshot {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final long[] values;

    /**
     * Create a new {@link Snapshot} with the given values.
     *
     * @param values    an unordered set of values in the reservoir
     */
    public UniformSnapshot(Collection<Long> values) {
        final Object[] copy = values.toArray();
        this.values = new long[copy.length];
        for (int i = 0; i < copy.length; i++) {
            this.values[i] = (Long) copy[i];
        }
        Arrays.sort(this.values);
    }

    /**
     * Create a new {@link Snapshot} with the given values.
     *
     * @param values    an unordered set of values in the reservoir
     */
    public UniformSnapshot(long[] values) {
        this.values = Arrays.copyOf(values, values.length);
        Arrays.sort(this.values);
    }

    /**
     * Returns the value at the given quantile.
     *
     * @param quantile    a given quantile, in {@code [0..1]}
     * @return the value in the distribution at {@code quantile}
     */
    @Override
    public double getValue(double quantile) {
        if (quantile < 0.0 || quantile > 1.0 || Double.isNaN( quantile )) {
            throw new IllegalArgumentException(quantile + " is not in [0..1]");
        }

        if (values.length == 0) {
            return 0.0;
        }

        final double pos = quantile * (values.length + 1);
        final int index = (int) pos;

        if (index < 1) {
            return values[0];
        }

        if (index >= values.length) {
            return values[values.length - 1];
        }

        final double lower = values[index - 1];
        final double upper = values[index];
        return lower + (pos - floor(pos)) * (upper - lower);
    }

    /**
     * Returns the number of values in the snapshot.
     *
     * @return the number of values
     */
    @Override
    public int size() {
        return values.length;
    }

    /**
     * Returns the entire set of values in the snapshot.
     *
     * @return the entire set of values
     */
    @Override
    public long[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    /**
     * Returns the highest value in the snapshot.
     *
     * @return the highest value
     */
    @Override
    public long getMax() {
        if (values.length == 0) {
            return 0;
        }
        return values[values.length - 1];
    }

    /**
     * Returns the lowest value in the snapshot.
     *
     * @return the lowest value
     */
    @Override
    public long getMin() {
        if (values.length == 0) {
            return 0;
        }
        return values[0];
    }

    /**
     * Returns the arithmetic mean of the values in the snapshot.
     *
     * @return the arithmetic mean
     */
    @Override
    public double getMean() {
        if (values.length == 0) {
            return 0;
        }

        double sum = 0;
        for (long value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    /**
     * Returns the standard deviation of the values in the snapshot.
     *
     * @return the standard deviation value
     */
    @Override
    public double getStdDev() {
        // two-pass algorithm for variance, avoids numeric overflow

        if (values.length <= 1) {
            return 0;
        }

        final double mean = getMean();
        double sum = 0;

        for (long value : values) {
            final double diff = value - mean;
            sum += diff * diff;
        }

        final double variance = sum / (values.length - 1);
        return Math.sqrt(variance);
    }

    /**
     * Writes the values of the snapshot to the given stream.
     *
     * @param output an output stream
     */
    @Override
    public void dump(OutputStream output) {
        final PrintWriter out = new PrintWriter(new OutputStreamWriter(output, UTF_8));
        try {
            for (long value : values) {
                out.printf("%d%n", value);
            }
        } finally {
            out.close();
        }
    }
}
