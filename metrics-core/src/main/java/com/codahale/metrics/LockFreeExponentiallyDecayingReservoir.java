package com.codahale.metrics;

import com.codahale.metrics.WeightedSnapshot.WeightedSample;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiConsumer;

/**
 * A lock-free exponentially-decaying random reservoir of {@code long}s. Uses Cormode et al's
 * forward-decaying priority reservoir sampling method to produce a statistically representative
 * sampling reservoir, exponentially biased towards newer entries.
 *
 * @see <a href="http://dimacs.rutgers.edu/~graham/pubs/papers/fwddecay.pdf">
 * Cormode et al. Forward Decay: A Practical Time Decay Model for Streaming Systems. ICDE '09:
 * Proceedings of the 2009 IEEE International Conference on Data Engineering (2009)</a>
 *
 * {@link LockFreeExponentiallyDecayingReservoir} is based closely on the {@link ExponentiallyDecayingReservoir},
 * however it provides looser guarantees while completely avoiding locks.
 *
 * Looser guarantees:
 * <ul>
 *     <li> Updates which occur concurrently with rescaling may be discarded if the orphaned state node is updated after
 *     rescale has replaced it. This condition has a greater probability as the rescale interval is reduced due to the
 *     increased frequency of rescaling. {@link #rescaleThresholdNanos} values below 30 seconds are not recommended.
 *     <li> Given a small rescale threshold, updates may attempt to rescale into a new bucket, but lose the CAS race
 *     and update into a newer bucket than expected. In these cases the measurement weight is reduced accordingly.
 *     <li>In the worst case, all concurrent threads updating the reservoir may attempt to rescale rather than
 *     a single thread holding an exclusive write lock. It's expected that the configuration is set such that
 *     rescaling is substantially less common than updating at peak load. Even so, when size is reasonably small
 *     it can be more efficient to rescale than to park and context switch.
 * </ul>
 *
 * @author <a href="mailto:ckozak@ckozak.net">Carter Kozak</a>
 */
public final class LockFreeExponentiallyDecayingReservoir implements Reservoir {

    private static final double SECONDS_PER_NANO = .000_000_001D;
    private static final AtomicReferenceFieldUpdater<LockFreeExponentiallyDecayingReservoir, State> stateUpdater =
            AtomicReferenceFieldUpdater.newUpdater(LockFreeExponentiallyDecayingReservoir.class, State.class, "state");

    private final int size;
    private final long rescaleThresholdNanos;
    private final Clock clock;

    private volatile State state;

    private static final class State {

        private static final AtomicIntegerFieldUpdater<State> countUpdater =
                AtomicIntegerFieldUpdater.newUpdater(State.class, "count");

        private final double alphaNanos;
        private final int size;
        private final long startTick;
        // Count is updated after samples are successfully added to the map.
        private final ConcurrentSkipListMap<Double, WeightedSample> values;

        private volatile int count;

        State(
                double alphaNanos,
                int size,
                long startTick,
                int count,
                ConcurrentSkipListMap<Double, WeightedSample> values) {
            this.alphaNanos = alphaNanos;
            this.size = size;
            this.startTick = startTick;
            this.values = values;
            this.count = count;
        }

        private void update(long value, long timestampNanos) {
            double itemWeight = weight(timestampNanos - startTick);
            double priority = itemWeight / ThreadLocalRandom.current().nextDouble();
            boolean mapIsFull = count >= size;
            if (!mapIsFull || values.firstKey() < priority) {
                addSample(priority, value, itemWeight, mapIsFull);
            }
        }

        private void addSample(double priority, long value, double itemWeight, boolean bypassIncrement) {
            if (values.putIfAbsent(priority, new WeightedSample(value, itemWeight)) == null
                    && (bypassIncrement || countUpdater.incrementAndGet(this) > size)) {
                values.pollFirstEntry();
            }
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
        State rescale(long newTick) {
            long durationNanos = newTick - startTick;
            double scalingFactor = Math.exp(-alphaNanos * durationNanos);
            int newCount = 0;
            ConcurrentSkipListMap<Double, WeightedSample> newValues = new ConcurrentSkipListMap<>();
            if (Double.compare(scalingFactor, 0) != 0) {
                RescalingConsumer consumer = new RescalingConsumer(scalingFactor, newValues);
                values.forEach(consumer);
                // make sure the counter is in sync with the number of stored samples.
                newCount = consumer.count;
            }
            // It's possible that more values were added while the map was scanned, those with the
            // minimum priorities are removed.
            while (newCount > size) {
                Objects.requireNonNull(newValues.pollFirstEntry(), "Expected an entry");
                newCount--;
            }
            return new State(alphaNanos, size, newTick, newCount, newValues);
        }

        private double weight(long durationNanos) {
            return Math.exp(alphaNanos * durationNanos);
        }
    }

    private static final class RescalingConsumer implements BiConsumer<Double, WeightedSample> {
        private final double scalingFactor;
        private final ConcurrentSkipListMap<Double, WeightedSample> values;
        private int count;

        RescalingConsumer(double scalingFactor, ConcurrentSkipListMap<Double, WeightedSample> values) {
            this.scalingFactor = scalingFactor;
            this.values = values;
        }

        @Override
        public void accept(Double priority, WeightedSample sample) {
            double newWeight = sample.weight * scalingFactor;
            if (Double.compare(newWeight, 0) == 0) {
                return;
            }
            WeightedSample newSample = new WeightedSample(sample.value, newWeight);
            if (values.put(priority * scalingFactor, newSample) == null) {
                count++;
            }
        }
    }

    private LockFreeExponentiallyDecayingReservoir(int size, double alpha, Duration rescaleThreshold, Clock clock) {
        // Scale alpha to nanoseconds
        double alphaNanos = alpha * SECONDS_PER_NANO;
        this.size = size;
        this.clock = clock;
        this.rescaleThresholdNanos = rescaleThreshold.toNanos();
        this.state = new State(alphaNanos, size, clock.getTick(), 0, new ConcurrentSkipListMap<>());
    }

    @Override
    public int size() {
        return Math.min(size, state.count);
    }

    @Override
    public void update(long value) {
        long now = clock.getTick();
        rescaleIfNeeded(now).update(value, now);
    }

    private State rescaleIfNeeded(long currentTick) {
        // This method is optimized for size so the check may be quickly inlined.
        // Rescaling occurs substantially less frequently than the check itself.
        State stateSnapshot = this.state;
        if (currentTick - stateSnapshot.startTick >= rescaleThresholdNanos) {
            return doRescale(currentTick, stateSnapshot);
        }
        return stateSnapshot;
    }

    private State doRescale(long currentTick, State stateSnapshot) {
        State newState = stateSnapshot.rescale(currentTick);
        if (stateUpdater.compareAndSet(this, stateSnapshot, newState)) {
            // newState successfully installed
            return newState;
        }
        // Otherwise another thread has won the race and we can return the result of a volatile read.
        // It's possible this has taken so long that another update is required, however that's unlikely
        // and no worse than the standard race between a rescale and update.
        return this.state;
    }

    @Override
    public Snapshot getSnapshot() {
        State stateSnapshot = rescaleIfNeeded(clock.getTick());
        return new WeightedSnapshot(stateSnapshot.values.values());
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * By default this uses a size of 1028 elements, which offers a 99.9%
     * confidence level with a 5% margin of error assuming a normal distribution, and an alpha
     * factor of 0.015, which heavily biases the reservoir to the past 5 minutes of measurements.
     */
    public static final class Builder {
        private static final int DEFAULT_SIZE = 1028;
        private static final double DEFAULT_ALPHA = 0.015D;
        private static final Duration DEFAULT_RESCALE_THRESHOLD = Duration.ofHours(1);

        private int size = DEFAULT_SIZE;
        private double alpha = DEFAULT_ALPHA;
        private Duration rescaleThreshold = DEFAULT_RESCALE_THRESHOLD;
        private Clock clock = Clock.defaultClock();

        private Builder() {}

        /**
         * Maximum number of samples to keep in the reservoir. Once this number is reached older samples are
         * replaced (based on weight, with some amount of random jitter).
         */
        public Builder size(int value) {
            if (value <= 0) {
                throw new IllegalArgumentException(
                        "LockFreeExponentiallyDecayingReservoir size must be positive: " + value);
            }
            this.size = value;
            return this;
        }

        /**
         * Alpha is the exponential decay factor. Higher values bias results more heavily toward newer values.
         */
        public Builder alpha(double value) {
            this.alpha = value;
            return this;
        }

        /**
         * Interval at which this reservoir is rescaled.
         */
        public Builder rescaleThreshold(Duration value) {
            this.rescaleThreshold = Objects.requireNonNull(value, "rescaleThreshold is required");
            return this;
        }

        /**
         * Clock instance used for decay.
         */
        public Builder clock(Clock value) {
            this.clock = Objects.requireNonNull(value, "clock is required");
            return this;
        }

        public Reservoir build() {
            return new LockFreeExponentiallyDecayingReservoir(size, alpha, rescaleThreshold, clock);
        }
    }
}
