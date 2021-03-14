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
public class UniformDoubleReservoir implements DoubleReservoir {
    private static final int DEFAULT_SIZE = 1028;
    private final AtomicLong count = new AtomicLong();
    private final AtomicLongArray values;

    /**
     * Creates a new {@link UniformDoubleReservoir} of 1028 elements, which offers a 99.9% confidence level
     * with a 5% margin of error assuming a normal distribution.
     */
    public UniformDoubleReservoir() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a new {@link UniformDoubleReservoir}.
     *
     * @param size the number of samples to keep in the sampling reservoir
     */
    public UniformDoubleReservoir(int size) {
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
    public void update(double value) {
        final long c = count.incrementAndGet();
        if (c <= values.length()) {
            values.set((int) c - 1, Double.doubleToLongBits(value));
        } else {
            final long r = ThreadLocalRandom.current().nextLong(c);
            if (r < values.length()) {
                values.set((int) r, Double.doubleToLongBits(value));
            }
        }
    }

    @Override
    public DoubleSnapshot getSnapshot() {
        final int s = size();
        double[] copy = new double[s];
        for (int i = 0; i < s; i++) {
            copy[i] = Double.longBitsToDouble(values.get(i));
        }
        return new UniformDoubleSnapshot(copy);
    }
}
