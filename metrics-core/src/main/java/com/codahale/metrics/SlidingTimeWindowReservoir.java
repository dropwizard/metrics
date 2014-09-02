package com.codahale.metrics;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Reservoir} implementation backed by a sliding window that stores only the measurements made
 * in the last {@code N} seconds (or other time unit).
 */
public class SlidingTimeWindowReservoir implements Reservoir {
    // allow for this many duplicate ticks before overwriting measurements
    private static final int COLLISION_BUFFER = 256;
    // only trim on updating once every N
    private static final int TRIM_THRESHOLD = 256;

    private final Clock clock;
    private final ConcurrentSkipListMap<Long, Long> measurements;
    private final long window;
    private final AtomicLong lastTick;
    private final AtomicLong count;

    /**
     * Creates a new {@link SlidingTimeWindowReservoir} with the given window of time.
     *
     * @param window     the window of time
     * @param windowUnit the unit of {@code window}
     */
    public SlidingTimeWindowReservoir(long window, TimeUnit windowUnit) {
        this(window, windowUnit, Clock.defaultClock());
    }

    /**
     * Creates a new {@link SlidingTimeWindowReservoir} with the given clock and window of time.
     *
     * @param window     the window of time
     * @param windowUnit the unit of {@code window}
     * @param clock      the {@link Clock} to use
     */
    public SlidingTimeWindowReservoir(long window, TimeUnit windowUnit, Clock clock) {
        this.clock = clock;
        this.measurements = new ConcurrentSkipListMap<Long, Long>();
        this.window = windowUnit.toNanos(window) * COLLISION_BUFFER;
        this.lastTick = new AtomicLong(clock.getTick() * COLLISION_BUFFER);
        this.count = new AtomicLong();
    }

    @Override
    public int size() {
        trim();
        return measurements.size();
    }

    @Override
    public void update(long value) {
        if (count.incrementAndGet() % TRIM_THRESHOLD == 0) {
            trim();
        }
        measurements.put(getTick(), value);
    }

    @Override
    public Snapshot getSnapshot() {
        trim();
        return new UniformSnapshot(measurements.values());
    }

    private long getTick() {
        for (; ; ) {
            final long oldTick = lastTick.get();
            final long tick = clock.getTick() * COLLISION_BUFFER;
            // ensure the tick is strictly incrementing even if there are duplicate ticks
            final long newTick = tick - oldTick > 0 ? tick : oldTick + 1;
            if (lastTick.compareAndSet(oldTick, newTick)) {
                return newTick;
            }
        }
    }

    private void trim() {
        measurements.headMap(getTick() - window).clear();
    }
}
