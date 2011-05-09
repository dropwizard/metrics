package com.yammer.metrics.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * A random sample of a stream of {@code long}s. Uses Vitter's Algorithm R to
 * produce a statistically representative sample.
 *
 * @author coda
 * @see <a href="http://www.cs.umd.edu/~samir/498/vitter.pdf">Random Sampling
 *      with a Reservoir</a>
 */
public class UniformSample implements Sample {
    private static final Random RANDOM = new Random();
    private final AtomicLong count = new AtomicLong();
    private final AtomicLongArray values;

    /**
     * Creates a new {@link UniformSample}.
     *
     * @param reservoirSize the number of samples to keep in the sampling
     *                      reservoir
     */
    public UniformSample(int reservoirSize) {
        this.values = new AtomicLongArray(reservoirSize);
        clear();
    }

    @Override
    public void clear() {
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
            final long r = Math.abs(RANDOM.nextLong()) % c;
            if (r < values.length()) {
                values.set((int) r, value);
            }
        }
    }

    @Override
    public List<Long> values() {
        final int s = size();
        final List<Long> copy = new ArrayList<Long>(s);
        for (int i = 0; i < s; i++) {
            copy.add(values.get(i));
        }
        return copy;
    }
}
