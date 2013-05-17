package com.codahale.metrics;

import static java.lang.Math.min;

/**
 * A {@link Reservoir} implementation backed by a sliding window that stores the last {@code N}
 * measurements.
 */
public class SlidingWindowReservoir implements Reservoir {
    private final long[] measurements;
    private long count;

    /**
     * Creates a new {@link SlidingWindowReservoir} which stores the last {@code size} measurements.
     *
     * @param size the number of measurements to store
     */
    public SlidingWindowReservoir(int size) {
        this.measurements = new long[size];
        this.count = 0;
    }

    @Override
    public synchronized int size() {
        return (int) min(count, measurements.length);
    }

    @Override
    public synchronized void update(long value) {
        measurements[((int) count++ % measurements.length)] = value;
    }

    @Override
    public Snapshot getSnapshot() {
        final long[] values = new long[size()];
        for (int i = 0; i < values.length; i++) {
            synchronized (this) {
                values[i] = measurements[i];
            }
        }
        return new Snapshot(values);
    }
}
