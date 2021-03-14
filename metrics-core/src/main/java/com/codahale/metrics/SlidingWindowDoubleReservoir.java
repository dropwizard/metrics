package com.codahale.metrics;

import static java.lang.Math.min;

/**
 * A {@link Reservoir} implementation backed by a sliding window that stores the last {@code N}
 * measurements.
 */
public class SlidingWindowDoubleReservoir implements DoubleReservoir {
    private final double[] measurements;
    private long count;

    /**
     * Creates a new {@link SlidingWindowDoubleReservoir} which stores the last {@code size} measurements.
     *
     * @param size the number of measurements to store
     */
    public SlidingWindowDoubleReservoir(int size) {
        this.measurements = new double[size];
        this.count = 0;
    }

    @Override
    public synchronized int size() {
        return (int) min(count, measurements.length);
    }

    @Override
    public synchronized void update(double value) {
        measurements[(int) (count++ % measurements.length)] = value;
    }

    @Override
    public DoubleSnapshot getSnapshot() {
        final double[] values = new double[size()];
        for (int i = 0; i < values.length; i++) {
            synchronized (this) {
                values[i] = measurements[i];
            }
        }
        return new UniformDoubleSnapshot(values);
    }
}
