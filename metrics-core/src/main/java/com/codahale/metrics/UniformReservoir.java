package com.codahale.metrics;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * A random sampling reservoir of a stream of {@code long}s. Uses Vitter's Algorithm R to produce a
 * statistically representative sample.
 *
 * @see <a href="http://www.cs.umd.edu/~samir/498/vitter.pdf">Random Sampling with a Reservoir</a>
 */
public class UniformReservoir implements Reservoir {
    private static final int DEFAULT_SIZE = 1028;
    private final AtomicLong count = new AtomicLong();
    private final AtomicLongArray values;

    /**
     * Creates a new {@link UniformReservoir} of 1028 elements, which offers a 99.9% confidence level
     * with a 5% margin of error assuming a normal distribution.
     */
    public UniformReservoir() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a new {@link UniformReservoir}.
     *
     * @param size the number of samples to keep in the sampling reservoir
     */
    public UniformReservoir(int size) {
        this.values = new AtomicLongArray(size);
        for (int i = 0; i < values.length(); i++) {
            values.set(i, 0);
        }
        count.set(0);
    }

    @Override
    public int size() {
        final long c = count.get();
        if (c > values.length()) {
            return values.length();
        }
        return (int) c;
    }

    @Override
    public void update(long value) {
        final long c = count.incrementAndGet();
        if (c <= values.length()) {
            values.set((int) c - 1, value);
        } else {
            final long r = ThreadLocalRandom.current().nextLong(c);
            if (r < values.length()) {
                values.set((int) r, value);
            }
        }
    }

    @Override
    public Snapshot getSnapshot() {
        final int s = size();
        long[] copy = new long[s];
        for (int i = 0; i < s; i++) {
            copy[i] = values.get(i);  
        }
        return new UniformSnapshot(copy);
    }
}
