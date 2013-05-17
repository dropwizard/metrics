package com.codahale.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import static java.lang.Math.min;

/**
 * A {@link Reservoir} implementation backed by a sliding window that stores the
 * last {@code N} measurements.
 */
public class SlidingWindowReservoir implements Reservoir {
    private final AtomicLong index;
    private final AtomicLong count;
    private final AtomicLongArray measurements;

    /**
     * Creates a new {@link SlidingWindowReservoir} which stores the last
     * {@code size} measurements.
     *
     * @param size the number of measurements to store
     */
    public SlidingWindowReservoir(int size) {
        this.index = new AtomicLong();
        this.count = new AtomicLong();
        this.measurements = new AtomicLongArray(size);
    }

    @Override
    public int size() {
        return (int) min(count.get(), measurements.length());
    }

    @Override
    public void update(long value) {
        // first, get and increment the index
        final int n = (int) (index.getAndIncrement() % measurements.length());
        // second, set the measurement
        measurements.set(n, value);
        // third, increment the count of written measurements
        count.incrementAndGet();
    }

    @Override
    public Snapshot getSnapshot() {
        // use size, not index, to prevent phantom reads
        final long[] values = new long[size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = measurements.get(i);
        }
        return new Snapshot(values);
    }
}
