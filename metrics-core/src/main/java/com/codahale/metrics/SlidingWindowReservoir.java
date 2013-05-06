package com.codahale.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import static java.lang.Math.min;

/**
 * A {@link Reservoir} implementation backed by a sliding window that stores the last {@code N}
 * measurements.
 */
public class SlidingWindowReservoir implements Reservoir {
    private final AtomicLongArray measurements;
    private final AtomicLong count;
    private final AtomicLong sequence;

    /**
     * Creates a new {@link SlidingWindowReservoir} which stores the last {@code size} measurements.
     *
     * @param size the number of measurements to store
     */
    public SlidingWindowReservoir(int size) {
        this.measurements = new AtomicLongArray(size);
        this.count = new AtomicLong();
        this.sequence = new AtomicLong();
    }

    @Override
    public int size() {
        return (int) min(count.get(), measurements.length());
    }

    @Override
    public void update(long value) {
        final int i = (int) (count.getAndIncrement() % measurements.length());
        measurements.set(i, value);
        sequence.incrementAndGet();
    }

    @Override
    public Snapshot getSnapshot() {
        while(count.get() != sequence.get()) {
            Thread.yield();
        }

        final long[] values = new long[size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = measurements.get(i);
        }
        return new Snapshot(values);
    }
}
