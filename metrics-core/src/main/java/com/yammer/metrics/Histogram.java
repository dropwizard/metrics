package com.yammer.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.sqrt;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately computing running
 *      variance</a>
 */
public class Histogram implements Metric, Sampling, Summarizable {
    private final Sample sample;
    private final AtomicLong min;
    private final AtomicLong max;
    private final AtomicLong sum;
    // These are for the Welford algorithm for calculating running variance
    // without floating-point doom.
    private final AtomicReference<double[]> variance; // M, S
    private final AtomicLong count;

    /**
     * Creates a new {@link Histogram} with the given sample type.
     *
     * @param type the type of sample to use
     */
    public Histogram(SampleType type) {
        this(type.newSample());
    }

    /**
     * Creates a new {@link Histogram} with the given sample.
     *
     * @param sample the sample to create a histogram from
     */
    public Histogram(Sample sample) {
        this.sample = sample;
        this.min = new AtomicLong(Long.MAX_VALUE);
        this.max = new AtomicLong(Long.MIN_VALUE);
        this.sum = new AtomicLong(0);
        this.variance = new AtomicReference<double[]>(new double[]{-1, 0});
        this.count = new AtomicLong(0);
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
    public long getCount() {
        return count.get();
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.Summarizable#max()
     */
    @Override
    public long getMax() {
        if (getCount() > 0) {
            return max.get();
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.Summarizable#min()
     */
    @Override
    public long getMin() {
        if (getCount() > 0) {
            return min.get();
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.Summarizable#mean()
     */
    @Override
    public double getMean() {
        if (getCount() > 0) {
            return sum.get() / (double) getCount();
        }
        return 0.0;
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.Summarizable#stdDev()
     */
    @Override
    public double getStdDev() {
        if (getCount() > 0) {
            return sqrt(getVariance());
        }
        return 0.0;
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.Summarizable#sum()
     */
    @Override
    public long getSum() {
        return sum.get();
    }

    @Override
    public Snapshot getSnapshot() {
        return sample.getSnapshot();
    }

    private double getVariance() {
        if (getCount() <= 1) {
            return 0.0;
        }
        return variance.get()[1] / (getCount() - 1);
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
        while (true) {
            final double[] oldValues = variance.get();
            final double[] newValues = new double[2];
            if (oldValues[0] == -1) {
                newValues[0] = value;
                newValues[1] = 0;
            } else {
                final double oldM = oldValues[0];
                final double oldS = oldValues[1];

                final double newM = oldM + ((value - oldM) / getCount());
                final double newS = oldS + ((value - oldM) * (value - newM));

                newValues[0] = newM;
                newValues[1] = newS;
            }
            if (variance.compareAndSet(oldValues, newValues)) {
                return;
            }
        }
    }
}
