package com.yammer.metrics.core;

import com.yammer.metrics.stats.ExponentiallyDecayingSample;
import com.yammer.metrics.stats.Sample;
import com.yammer.metrics.stats.UniformSample;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately computing running
 *      variance</a>
 */
public class Histogram implements Metric, Quantized, Summarized {
    /**
     * The type of sampling the histogram should be performing.
     */
    enum SampleType {
        /**
         * Uses a uniform sample of 1028 elements, which offers a 99.9% confidence level with a 5%
         * margin of error assuming a normal distribution.
         */
        UNIFORM {
            @Override
            public Sample newSample() {
                return new UniformSample(1028);
            }
        },

        /**
         * Uses an exponentially decaying sample of 1028 elements, which offers a 99.9% confidence
         * level with a 5% margin of error assuming a normal distribution, and an alpha factor of
         * 0.015, which heavily biases the sample to the past 5 minutes of measurements.
         */
        BIASED {
            @Override
            public Sample newSample() {
                return new ExponentiallyDecayingSample(1028, 0.015);
            }
        };

        public abstract Sample newSample();
    }

    /**
     * Cache arrays for the variance calculation, so as to avoid memory allocation.
     */
    private static class ArrayCache extends ThreadLocal<double[]> {
        @Override
        protected double[] initialValue() {
            return new double[2];
        }
    }

    private final Sample sample;
    private final AtomicLong min = new AtomicLong();
    private final AtomicLong max = new AtomicLong();
    private final AtomicLong sum = new AtomicLong();
    // These are for the Welford algorithm for calculating running variance
    // without floating-point doom.
    private final AtomicReference<double[]> variance =
            new AtomicReference<double[]>(new double[]{-1, 0}); // M, S
    private final AtomicLong count = new AtomicLong();
    private final ArrayCache arrayCache = new ArrayCache();

    /**
     * Creates a new {@link Histogram} with the given sample type.
     *
     * @param type the type of sample to use
     */
    Histogram(SampleType type) {
        this(type.newSample());
    }

    /**
     * Creates a new {@link Histogram} with the given sample.
     *
     * @param sample the sample to create a histogram from
     */
    Histogram(Sample sample) {
        this.sample = sample;
        clear();
    }

    /**
     * Clears all recorded values.
     */
    public void clear() {
        sample.clear();
        count.set(0);
        max.set(Long.MIN_VALUE);
        min.set(Long.MAX_VALUE);
        sum.set(0);
        variance.set(new double[]{-1, 0});
    }

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    public void update(int value) {
        update((long) value);
    }

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    public void update(long value) {
        count.incrementAndGet();
        sample.update(value);
        setMax(value);
        setMin(value);
        sum.getAndAdd(value);
        updateVariance(value);
    }

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    public long count() {
        return count.get();
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.core.Summarized#max()
     */
    @Override
    public double max() {
        if (count() > 0) {
            return max.get();
        }
        return 0.0;
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.core.Summarized#min()
     */
    @Override
    public double min() {
        if (count() > 0) {
            return min.get();
        }
        return 0.0;
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.core.Summarized#mean()
     */
    @Override
    public double mean() {
        if (count() > 0) {
            return sum.get() / (double) count();
        }
        return 0.0;
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.core.Summarized#stdDev()
     */
    @Override
    public double stdDev() {
        if (count() > 0) {
            return sqrt(variance());
        }
        return 0.0;
    }

    /**
     * Returns the value at the given quantile.
     *
     * @param quantile a quantile ({@code 0..1})
     * @return the value at the given quantile
     */
    @Override
    public double quantile(double quantile) {
        return quantiles(quantile)[0];
    }

    /**
     * Returns an array of values at the given quantiles.
     *
     * @param quantiles one or more quantiles ({@code 0..1})
     * @return an array of values at the given quantiles
     */
    @Override
    public Double[] quantiles(Double... quantiles) {
        final Double[] scores = new Double[quantiles.length];
        for (int i = 0; i < scores.length; i++) {
            scores[i] = 0.0;

        }

        if (count() > 0) {
            final List<Long> values = sample.values();
            Collections.sort(values);

            for (int i = 0; i < quantiles.length; i++) {
                final double p = quantiles[i];
                final double pos = p * (values.size() + 1);
                if (pos < 1) {
                    scores[i] = Double.valueOf(values.get(0));
                } else if (pos >= values.size()) {
                    scores[i] = Double.valueOf(values.get(values.size() - 1));
                } else {
                    final double lower = values.get((int) pos - 1);
                    final double upper = values.get((int) pos);
                    scores[i] = lower + (pos - floor(pos)) * (upper - lower);
                }
            }
        }

        return scores;
    }

    /**
     * Returns a list of all values in the histogram's sample.
     *
     * @return a list of all values in the histogram's sample
     */
    public List<Long> values() {
        return sample.values();
    }

    /**
     * Writes the values of the histogram's sample to the given file.
     *
     * @param output the file to which the values will be written
     * @throws IOException if there is an error writing the values
     */
    public void dump(File output) throws IOException {
        sample.dump(output);
    }

    private double variance() {
        if (count() <= 1) {
            return 0.0;
        }
        return variance.get()[1] / (count() - 1);
    }

    private void setMax(long potentialMax) {
        boolean done = false;
        while (!done) {
            final long currentMax = max.get();
            done = currentMax >= potentialMax || max.compareAndSet(currentMax, potentialMax);
        }
    }

    private void setMin(long potentialMin) {
        boolean done = false;
        while (!done) {
            final long currentMin = min.get();
            done = currentMin <= potentialMin || min.compareAndSet(currentMin, potentialMin);
        }
    }

    private void updateVariance(long value) {
        boolean done = false;
        while (!done) {
            final double[] oldValues = variance.get();
            final double[] newValues = arrayCache.get();
            if (oldValues[0] == -1) {
                newValues[0] = value;
                newValues[1] = 0;
            } else {
                final double oldM = oldValues[0];
                final double oldS = oldValues[1];

                final double newM = oldM + ((value - oldM) / count());
                final double newS = oldS + ((value - oldM) * (value - newM));

                newValues[0] = newM;
                newValues[1] = newS;
            }
            done = variance.compareAndSet(oldValues, newValues);
            if (done) {
                // recycle the old array into the cache
                arrayCache.set(oldValues);
            }
        }
    }

    @Override
    public <T> void processWith(MetricProcessor<T> processor, MetricName name, T context) throws Exception {
        processor.processHistogram(name, this, context);
    }
}
