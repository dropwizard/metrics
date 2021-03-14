package com.codahale.metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately computing running
 * variance</a>
 */
public class DoubleHistogram implements Metric, DoubleSampling, Counting {
    private final DoubleReservoir reservoir;
    private final LongAdder count;

    /**
     * Creates a new {@link DoubleHistogram} with the given reservoir.
     *
     * @param reservoir the reservoir to create a histogram from
     */
    public DoubleHistogram(DoubleReservoir reservoir) {
        this.reservoir = reservoir;
        this.count = new LongAdder();
    }

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    public void update(int value) {
        update((double) value);
    }

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    public void update(long value) {
        update((double) value);
    }

    /**
     * Adds a recorded value.
     *
     * @param value the length of the value
     */
    public void update(double value) {
        count.increment();
        reservoir.update(value);
    }

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    @Override
    public long getCount() {
        return count.sum();
    }

    @Override
    public DoubleSnapshot getSnapshot() {
        return reservoir.getSnapshot();
    }
}
