package com.yammer.metrics.core;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

import com.yammer.metrics.stats.ExponentiallyDecayingSample;
import com.yammer.metrics.stats.Sample;
import com.yammer.metrics.stats.UniformSample;

/**
 * A metric which calculates the distribution of a value.
 *
 * @author coda
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately
 * computing running variance</a>
 */
public class HistogramMetric implements Metric {
    /**
     * The type of sampling the histogram should be performing.
     */
    public enum SampleType {
        /**
         * Uses a uniform sample of 1028 elements, which offers a 99.9%
         * confidence level with a 5% margin of error assuming a normal
         * distribution.
         */
        UNIFORM {
            @Override
            public Sample newSample() {
                return new UniformSample(1028);
            }
        },

        /**
         * Uses an exponentially decaying sample of 1028 elements, which offers
         * a 99.9% confidence level with a 5% margin of error assuming a normal
         * distribution, and an alpha factor of 0.015, which heavily biases
         * the sample to the past 5 minutes of measurements.
         */
        BIASED {
            @Override
            public Sample newSample() {
                return new ExponentiallyDecayingSample(1028, 0.015);
            }
        };

        public abstract Sample newSample();
    }

    private final Sample sample;
    private final AtomicLong _min = new AtomicLong();
    private final AtomicLong _max = new AtomicLong();
    private final AtomicLong _sum = new AtomicLong();
    // These are for the Welford algorithm for calculating running variance
    // without floating-point doom.
    private final AtomicLong varianceM = new AtomicLong();
    private final AtomicLong varianceS = new AtomicLong();
    private final AtomicLong count = new AtomicLong();

    /**
     * Creates a new {@link HistogramMetric} with the given sample type.
     *
     * @param type the type of sample to use
     */
    public HistogramMetric(SampleType type) {
        this(type.newSample());
    }

    /**
     * Creates a new {@link HistogramMetric} with the given sample.
     *
     * @param sample the sample to create a histogram from
     */
    public HistogramMetric(Sample sample) {
        this.sample = sample;
        clear();
    }

    /**
     * Clears all recorded values.
     */
    public void clear() {
        sample.clear();
        count.set(0);
        _max.set(Long.MIN_VALUE);
        _min.set(Long.MAX_VALUE);
        _sum.set(0);
        varianceM.set(-1);
        varianceS.set(0);
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
        _sum.getAndAdd(value);
        updateVariance(value);
    }

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    public long count() { return count.get(); }

    /**
     * Returns the largest recorded value.
     *
     * @return the largest recorded value
     */
    public double max() {
        if (count() > 0) {
            return _max.get();
        }
        return 0.0;
    }

    /**
     * Returns the smallest recorded value.
     *
     * @return the smallest recorded value
     */
    public double min() {
        if (count() > 0) {
            return _min.get();
        }
        return 0.0;
    }

    /**
     * Returns the arithmetic mean of all recorded values.
     *
     * @return the arithmetic mean of all recorded values
     */
    public double mean() {
        if (count() > 0) {
            return _sum.get() / (double) count();
        }
        return 0.0;
    }

    /**
     * Returns the standard deviation of all recorded values.
     *
     * @return the standard deviation of all recorded values
     */
    public double stdDev() {
        if (count() > 0) {
            return sqrt(variance());
        }
        return 0.0;
    }

    /**
     * Returns an array of values at the given percentiles.
     *
     * @param percentiles one or more percentiles ({@code 0..1})
     * @return an array of values at the given percentiles
     */
    public double[] percentiles(double... percentiles) {
        final double[] scores = new double[percentiles.length];
        for (int i = 0; i < scores.length; i++) {
            scores[i] = 0.0;

        }

        if (count() > 0) {
            final List<Long> values = sample.values();
            Collections.sort(values);

            for (int i = 0; i < percentiles.length; i++) {
                final double p = percentiles[i];
                final double pos = p * (values.size() + 1);
                if (pos < 1) {
                    scores[i] = values.get(0);
                } else if (pos >= values.size()) {
                    scores[i] = values.get(values.size() - 1);
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

    private double variance() {
        if (count() <= 1) {
            return 0.0;
        }
        return longBitsToDouble(varianceS.get()) / (count() - 1);
    }

    private void setMax(long potentialMax) {
        boolean done = false;
        while (!done) {
            long currentMax = _max.get();
            done = currentMax >= potentialMax || _max.compareAndSet(currentMax, potentialMax);
        }
    }

    private void setMin(long potentialMin) {
        boolean done = false;
        while (!done) {
            long currentMin = _min.get();
            done = currentMin <= potentialMin || _min.compareAndSet(currentMin, potentialMin);
        }
    }

    private void updateVariance(long value) {
        // initialize varianceM to the first reading if it's still blank
        if (!varianceM.compareAndSet(-1, doubleToLongBits(value))) {
            boolean done = false;
            while (!done) {
                final long oldMCas = varianceM.get();
                final double oldM = longBitsToDouble(oldMCas);
                final double newM = oldM + ((value - oldM) / count());

                final long oldSCas = varianceS.get();
                final double oldS = longBitsToDouble(oldSCas);
                final double newS = oldS + ((value - oldM) * (value - newM));

                done = varianceM.compareAndSet(oldMCas, doubleToLongBits(newM)) &&
                        varianceS.compareAndSet(oldSCas, doubleToLongBits(newS));
            }
        }
    }
}
