package com.yammer.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import static java.lang.Math.min;

/**
 * A {@link Sample} implementation backed by a moving window that stores the last {@code N}
 * measurements.
 */
public class MovingWindowSample implements Sample {
    private final AtomicLongArray window;
    private final AtomicLong index;

    /**
     * Creates a new {@link MovingWindowSample} which stores the last {@code size} measurements.
     *
     * @param size the number of measurements to store
     */
    public MovingWindowSample(int size) {
        this.window = new AtomicLongArray(size);
        this.index = new AtomicLong();
    }

    @Override
    public void clear() {
        index.set(0);
    }

    @Override
    public int size() {
        return (int) min(index.get(), window.length());
    }

    @Override
    public void update(long value) {
        final int i = (int) (index.getAndIncrement() % window.length());
        window.set(i, value);
    }

    @Override
    public Snapshot getSnapshot() {
        final long[] values = new long[size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = window.get(i);
        }
        return new Snapshot(values);
    }
}
