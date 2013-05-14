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
        // use count, not index, to prevent phantom reads
        return (int) min(count.get(), measurements.length());
    }

    @Override
    public void update(long value) {
        // entry barrier
        final long n = index.getAndIncrement();
        final int idx = (int) (n % measurements.length());

        // critical section
        measurements.set(idx, value);

        // exit barrier
        while (true) {
            // Only return once other writers (i.e., those who have already incremented index) have
            // incremented count.
            if (count.compareAndSet(n, n + 1)) {
                return;
            }
        }
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
