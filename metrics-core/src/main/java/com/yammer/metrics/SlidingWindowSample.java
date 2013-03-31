package com.yammer.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import static java.lang.Math.min;

/**
 * A {@link Sample} implementation backed by a sliding window that stores the last {@code N}
 * measurements.
 */
public class SlidingWindowSample implements Sample {
    private final AtomicLongArray measurements;
    private final AtomicLong count;

    /**
     * Creates a new {@link SlidingWindowSample} which stores the last {@code size} measurements.
     *
     * @param size the number of measurements to store
     */
    public SlidingWindowSample(int size) {
        this.measurements = new AtomicLongArray(size);
        this.count = new AtomicLong();
    }

    @Override
    public int size() {
        return (int) min(count.get(), measurements.length());
    }

    @Override
    public void update(long value) {
        final int i = (int) (count.getAndIncrement() % measurements.length());
        measurements.set(i, value);
    }

    @Override
    public Snapshot getSnapshot() {
        final long[] values = new long[size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = measurements.get(i);
        }
        return new Snapshot(values);
    }
}
