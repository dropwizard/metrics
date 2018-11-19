package com.codahale.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * A triple of simple moving average rates (one, five and fifteen minutes rates) as needed by {@link Meter}.
 * <p>
 * The averages are unweighted, i.e. they include strictly only the events in the
 * sliding time window, every event having the same weight. Unlike the
 * the more widely used {@link ExponentialMovingAverages} implementation,
 * with this class the moving average rate drops immediately to zero if the last
 * marked event is older than the time window.
 * <p>
 * A {@link Meter} with {@link SlidingTimeWindowMovingAverages} works similarly to
 * a {@link Histogram} with an {@link SlidingTimeWindowArrayReservoir}, but as a Meter
 * needs to keep track only of the count of events (not the events itself), the memory
 * overhead is much smaller. SlidingTimeWindowMovingAverages uses buckets with just one
 * counter to accumulate the number of events (one bucket per seconds, giving 900 buckets
 * for the 15 minutes time window).
 */
public class SlidingTimeWindowMovingAverages implements MovingAverages {

    private static final long TIME_WINDOW_DURATION_MINUTES = 15;
    private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(1);
    private static final Duration TIME_WINDOW_DURATION = Duration.ofMinutes(TIME_WINDOW_DURATION_MINUTES);

    // package private for the benefit of the unit test
    static final int NUMBER_OF_BUCKETS = (int) (TIME_WINDOW_DURATION.toNanos() / TICK_INTERVAL);

    private final AtomicLong lastTick;
    private final Clock clock;

    private ArrayList<LongAdder> buckets;
    private int oldestBucketIndex;
    private int currentBucketIndex;
    private final Instant bucketBaseTime;
    Instant oldestBucketTime;

    /**
     * Creates a new {@link SlidingTimeWindowMovingAverages}.
     */
    public SlidingTimeWindowMovingAverages() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link SlidingTimeWindowMovingAverages}.
     *
     * @param clock the clock to use for the meter ticks
     */
    public SlidingTimeWindowMovingAverages(Clock clock) {
        this.clock = clock;
        final long startTime = clock.getTick();
        lastTick = new AtomicLong(startTime);

        buckets = new ArrayList<>(NUMBER_OF_BUCKETS);
        for (int i = 0; i < NUMBER_OF_BUCKETS; i++) {
            buckets.add(new LongAdder());
        }
        bucketBaseTime = Instant.ofEpochSecond(0L, startTime);
        oldestBucketTime = bucketBaseTime;
        oldestBucketIndex = 0;
        currentBucketIndex = 0;
    }

    @Override
    public void update(long n) {
        buckets.get(currentBucketIndex).add(n);
    }

    @Override
    public void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age >= TICK_INTERVAL) {
            final long newLastTick = newTick - age % TICK_INTERVAL;
            if (lastTick.compareAndSet(oldTick, newLastTick)) {
                Instant currentInstant = Instant.ofEpochSecond(0L, newLastTick);
                currentBucketIndex = normalizeIndex(calculateIndexOfTick(currentInstant));
                cleanOldBuckets(currentInstant);
            }
        }
    }

    @Override
    public double getM15Rate() {
        Instant now = Instant.ofEpochSecond(0L, lastTick.get());
        return sumBuckets(now, (int) (TimeUnit.MINUTES.toNanos(15) / TICK_INTERVAL));
    }

    @Override
    public double getM5Rate() {
        Instant now = Instant.ofEpochSecond(0L, lastTick.get());
        return sumBuckets(now, (int) (TimeUnit.MINUTES.toNanos(5) / TICK_INTERVAL));
    }

    @Override
    public double getM1Rate() {
        Instant now = Instant.ofEpochSecond(0L, lastTick.get());
        return sumBuckets(now, (int) (TimeUnit.MINUTES.toNanos(1) / TICK_INTERVAL));
    }

    int calculateIndexOfTick(Instant tickTime) {
        return (int) (Duration.between(bucketBaseTime, tickTime).toNanos() / TICK_INTERVAL);
    }

    int normalizeIndex(int index) {
        int mod = index % NUMBER_OF_BUCKETS;
        return mod >= 0 ? mod : mod + NUMBER_OF_BUCKETS;
    }

    private void cleanOldBuckets(Instant currentTick) {
        int newOldestIndex;
        Instant oldestStillNeededTime = currentTick.minus(TIME_WINDOW_DURATION).plusNanos(TICK_INTERVAL);
        Instant youngestNotInWindow = oldestBucketTime.plus(TIME_WINDOW_DURATION);
        if (oldestStillNeededTime.isAfter(youngestNotInWindow)) {
            newOldestIndex = oldestBucketIndex;
            oldestBucketTime = currentTick;
        } else if (oldestStillNeededTime.isAfter(oldestBucketTime)) {
            newOldestIndex = normalizeIndex(calculateIndexOfTick(oldestStillNeededTime));
            oldestBucketTime = oldestStillNeededTime;
        } else {
            return;
        }

        if (oldestBucketIndex < newOldestIndex) {
            for (int i = oldestBucketIndex; i < newOldestIndex; i++) {
                buckets.get(i).reset();
            }
        } else {
            for (int i = oldestBucketIndex; i < NUMBER_OF_BUCKETS; i++) {
                buckets.get(i).reset();
            }
            for (int i = 0; i < newOldestIndex; i++) {
                buckets.get(i).reset();
            }
        }
        oldestBucketIndex = newOldestIndex;
    }

    private long sumBuckets(Instant toTime, int numberOfBuckets) {

        // increment toIndex to include the current bucket into the sum
        int toIndex = normalizeIndex(calculateIndexOfTick(toTime) + 1);
        int fromIndex = normalizeIndex(toIndex - numberOfBuckets);
        LongAdder adder = new LongAdder();

        if (fromIndex < toIndex) {
            buckets.stream()
                    .skip(fromIndex)
                    .limit(toIndex - fromIndex)
                    .mapToLong(LongAdder::longValue)
                    .forEach(adder::add);
        } else {
            buckets.stream().limit(toIndex).mapToLong(LongAdder::longValue).forEach(adder::add);
            buckets.stream().skip(fromIndex).mapToLong(LongAdder::longValue).forEach(adder::add);
        }
        long retval = adder.longValue();
        return retval;
    }
}
