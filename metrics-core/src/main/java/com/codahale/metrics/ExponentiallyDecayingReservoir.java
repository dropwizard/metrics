package com.codahale.metrics;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Math.exp;
import static java.lang.Math.min;

import com.codahale.metrics.WeightedSnapshot.WeightedSample;

/**
 * An exponentially-decaying random reservoir of {@code long}s. Uses Cormode et al's
 * forward-decaying priority reservoir sampling method to produce a statistically representative
 * sampling reservoir, exponentially biased towards newer entries.
 *
 * @see <a href="http://dimacs.rutgers.edu/~graham/pubs/papers/fwddecay.pdf">
 * Cormode et al. Forward Decay: A Practical Time Decay Model for Streaming Systems. ICDE '09:
 *      Proceedings of the 2009 IEEE International Conference on Data Engineering (2009)</a>
 */
public class ExponentiallyDecayingReservoir implements Reservoir {
    private static final int DEFAULT_SIZE = 1028;
    private static final double DEFAULT_ALPHA = 0.015;
    private static final long RESCALE_THRESHOLD = TimeUnit.HOURS.toNanos(1);

    private final ConcurrentSkipListMap<Double, WeightedSample> values;
    private final ReentrantReadWriteLock lock;
    private final double alpha;
    private final int size;
    private final AtomicLong count;
    private volatile long startTime;
    private final AtomicLong nextScaleTime;
    private final Clock clock;

    /**
     * Creates a new {@link ExponentiallyDecayingReservoir} of 1028 elements, which offers a 99.9%
     * confidence level with a 5% margin of error assuming a normal distribution, and an alpha
     * factor of 0.015, which heavily biases the reservoir to the past 5 minutes of measurements.
     */
    public ExponentiallyDecayingReservoir() {
        this(DEFAULT_SIZE, DEFAULT_ALPHA);
    }

    /**
     * Creates a new {@link ExponentiallyDecayingReservoir}.
     *
     * @param size  the number of samples to keep in the sampling reservoir
     * @param alpha the exponential decay factor; the higher this is, the more biased the reservoir
     *              will be towards newer values
     */
    public ExponentiallyDecayingReservoir(int size, double alpha) {
        this(size, alpha, Clock.defaultClock());
    }

    /**
     * Creates a new {@link ExponentiallyDecayingReservoir}.
     *
     * @param size  the number of samples to keep in the sampling reservoir
     * @param alpha the exponential decay factor; the higher this is, the more biased the reservoir
     *              will be towards newer values
     * @param clock the clock used to timestamp samples and track rescaling
     */
    public ExponentiallyDecayingReservoir(int size, double alpha, Clock clock) {
        this.values = new ConcurrentSkipListMap<Double, WeightedSample>();
        this.lock = new ReentrantReadWriteLock();
        this.alpha = alpha;
        this.size = size;
        this.clock = clock;
        this.count = new AtomicLong(0);
        this.startTime = currentTimeInSeconds();
        this.nextScaleTime = new AtomicLong(clock.getTick() + RESCALE_THRESHOLD);
    }

    @Override
    public int size() {
        return (int) min(size, count.get());
    }

    @Override
    public void update(long value) {
        update(value, currentTimeInSeconds());
    }

    /**
     * Adds an old value with a fixed timestamp to the reservoir.
     *
     * @param value     the value to be added
     * @param timestamp the epoch timestamp of {@code value} in seconds
     */
    public void update(long value, long timestamp) {
        rescaleIfNeeded();
        lockForRegularUsage();
        try {
            final double itemWeight = weight(timestamp - startTime);
            final WeightedSample sample = new WeightedSample(value, itemWeight);
            final double priority = itemWeight / ThreadLocalRandom.current().nextDouble();

            final long newCount = count.incrementAndGet();
            if (newCount <= size) {
                values.put(priority, sample);
            } else {
                Double first = values.firstKey();
                if (first < priority && values.putIfAbsent(priority, sample) == null) {
                    // ensure we always remove an item
                    while (values.remove(first) == null) {
                        first = values.firstKey();
                    }
                }
            }
        } finally {
            unlockForRegularUsage();
        }
    }

    private void rescaleIfNeeded() {
        final long now = clock.getTick();
        final long next = nextScaleTime.get();
        if (now >= next) {
            rescale(now, next);
        }
    }

    @Override
    public Snapshot getSnapshot() {
        lockForRegularUsage();
        try {
            return new WeightedSnapshot(values.values());
        } finally {
            unlockForRegularUsage();
        }
    }

    private long currentTimeInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(clock.getTime());
    }

    private double weight(long t) {
        return exp(alpha * t);
    }

    /* "A common feature of the above techniques—indeed, the key technique that
     * allows us to track the decayed weights efficiently—is that they maintain
     * counts and other quantities based on g(ti − L), and only scale by g(t − L)
     * at query time. But while g(ti −L)/g(t−L) is guaranteed to lie between zero
     * and one, the intermediate values of g(ti − L) could become very large. For
     * polynomial functions, these values should not grow too large, and should be
     * effectively represented in practice by floating point values without loss of
     * precision. For exponential functions, these values could grow quite large as
     * new values of (ti − L) become large, and potentially exceed the capacity of
     * common floating point types. However, since the values stored by the
     * algorithms are linear combinations of g values (scaled sums), they can be
     * rescaled relative to a new landmark. That is, by the analysis of exponential
     * decay in Section III-A, the choice of L does not affect the final result. We
     * can therefore multiply each value based on L by a factor of exp(−α(L′ − L)),
     * and obtain the correct value as if we had instead computed relative to a new
     * landmark L′ (and then use this new L′ at query time). This can be done with
     * a linear pass over whatever data structure is being used."
     */
    private void rescale(long now, long next) {
        if (nextScaleTime.compareAndSet(next, now + RESCALE_THRESHOLD)) {
            lockForRescale();
            try {
                final long oldStartTime = startTime;
                this.startTime = currentTimeInSeconds();
                final double scalingFactor = exp(-alpha * (startTime - oldStartTime));

                final ArrayList<Double> keys = new ArrayList<Double>(values.keySet());
                for (Double key : keys) {
                    final WeightedSample sample = values.remove(key);
                    final WeightedSample newSample = new WeightedSample(sample.value, sample.weight * scalingFactor);
                    values.put(key * scalingFactor, newSample);
                }

                // make sure the counter is in sync with the number of stored samples.
                count.set(values.size());
            } finally {
                unlockForRescale();
            }
        }
    }

    private void unlockForRescale() {
        lock.writeLock().unlock();
    }

    private void lockForRescale() {
        lock.writeLock().lock();
    }

    private void lockForRegularUsage() {
        lock.readLock().lock();
    }

    private void unlockForRegularUsage() {
        lock.readLock().unlock();
    }
}
