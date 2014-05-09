package com.codahale.metrics;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * A statistical snapshot of a {@link WeightedSnapshot}.
 */
public class WeightedSnapshot extends Snapshot {
    
    /**
     * A single sample item with value and its weights for {@link WeightedSnapshot}.
     */
    public static class WeightedSample {
        public final long value;
        public final double weight;

        public WeightedSample(long value, double weight) {
            this.value = value;
            this.weight = weight;
        }
    }
    
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final long[] values;
    private final double[] normWeights;
    private final double[] quantiles;

    /**
     * Create a new {@link Snapshot} with the given values.
     *
     * @param values    an unordered set of values in the reservoir
     */
    public WeightedSnapshot(Collection<WeightedSample> values) {
        final WeightedSample[] copy = values.toArray( new WeightedSample[]{} );
    
        Arrays.sort(copy, new Comparator<WeightedSample>() {
            @Override
            public int compare(WeightedSample o1, WeightedSample o2) {
                if (o1.value > o2.value)
                    return 1;
                if (o1.value < o2.value)
                    return -1;
                return 0;
            }
        }
        );

        this.values = new long[copy.length];
        this.normWeights = new double[copy.length];
        this.quantiles = new double[copy.length];
        
        double sumWeight = 0;
        for (WeightedSample sample : copy) {
            sumWeight += sample.weight;
        }

        for (int i = 0; i < copy.length; i++) {
            this.values[i] = copy[i].value;
            this.normWeights[i] = copy[i].weight / sumWeight;
        }

        for (int i = 1; i < copy.length; i++) {
            this.quantiles[i] = this.quantiles[i - 1] + this.normWeights[i - 1];
        }
    }

    /**
     * Returns the value at the given quantile.
     *
     * @param quantile    a given quantile, in {@code [0..1]}
     * @return the value in the distribution at {@code quantile}
     */
    @Override
    public double getValue(double quantile) {
        if (quantile < 0.0 || quantile > 1.0) {
            throw new IllegalArgumentException(quantile + " is not in [0..1]");
        }

        if (values.length == 0) {
            return 0.0;
        }

        int posx = Arrays.binarySearch(quantiles, quantile);
        if (posx < 0)
            posx = ((-posx) - 1) - 1;

        if (posx < 1) {
            return values[0];
        }

        if (posx >= values.length) {
            return values[values.length - 1];
        }

        return values[(int) posx];
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
     * Returns the weighted arithmetic mean of the values in the snapshot.
     *
     * @return the weighted arithmetic mean
     */
    @Override
    public double getMean() {
        if (values.length == 0) {
            return 0;
        }

        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i] * normWeights[i];
        }
        return sum;
    }

    /**
     * Returns the weighted standard deviation of the values in the snapshot.
     *
     * @return the weighted standard deviation value
     */
    @Override
    public double getStdDev() {
        // two-pass algorithm for variance, avoids numeric overflow

        if (values.length <= 1) {
            return 0;
        }

        final double mean = getMean();
        double variance = 0;

        for (int i = 0; i < values.length; i++) {
            final double diff = values[i] - mean;
            variance += normWeights[i] * diff*diff;
        }

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
