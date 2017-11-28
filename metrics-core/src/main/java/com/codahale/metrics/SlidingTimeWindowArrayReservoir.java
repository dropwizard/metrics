package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Reservoir} implementation backed by a sliding window that stores only the measurements made
 * in the last {@code N} seconds (or other time unit).
 */
public class SlidingTimeWindowArrayReservoir implements Reservoir {
    // allow for this many duplicate ticks before overwriting measurements
    private static final long COLLISION_BUFFER = 256L;
    // only trim on updating once every N
    private static final long TRIM_THRESHOLD = 256L;
    private static final long CLEAR_BUFFER = TimeUnit.HOURS.toNanos(1) * COLLISION_BUFFER;

    private final Clock clock;
    private final ChunkedAssociativeLongArray measurements;
    private final long window;
    private final AtomicLong lastTick;
    private final AtomicLong count;
    private final long startTick;

    /**
     * Creates a new {@link SlidingTimeWindowArrayReservoir} with the given window of time.
     *
     * @param window     the window of time
     * @param windowUnit the unit of {@code window}
     */
    public SlidingTimeWindowArrayReservoir(long window, TimeUnit windowUnit) {
        this(window, windowUnit, Clock.defaultClock());
    }

    /**
     * Creates a new {@link SlidingTimeWindowArrayReservoir} with the given clock and window of time.
     *
     * @param window     the window of time
     * @param windowUnit the unit of {@code window}
     * @param clock      the {@link Clock} to use
     */
    public SlidingTimeWindowArrayReservoir(long window, TimeUnit windowUnit, Clock clock) {
        this.startTick = clock.getTick();
        this.clock = clock;
        this.measurements = new ChunkedAssociativeLongArray();
        this.window = windowUnit.toNanos(window) * COLLISION_BUFFER;
        this.lastTick = new AtomicLong((clock.getTick() - startTick) * COLLISION_BUFFER);
        this.count = new AtomicLong();
    }

    @Override
    public int size() {
        trim();
        return measurements.size();
    }

    @Override
    public void update(long value) {
        long newTick;
        do {
            if (count.incrementAndGet() % TRIM_THRESHOLD == 0L) {
                trim();
            }
            long lastTick = this.lastTick.get();
            newTick = getTick();
            boolean longOverflow = newTick < lastTick;
            if (longOverflow) {
                measurements.clear();
            }
        } while (!measurements.put(newTick, value));
    }

    @Override
    public Snapshot getSnapshot() {
        trim();
        return new UniformSnapshot(measurements.values());
    }

    private long getTick() {
        for ( ;; ) {
            final long oldTick = lastTick.get();
            final long tick = (clock.getTick() - startTick) * COLLISION_BUFFER;
            // ensure the tick is strictly incrementing even if there are duplicate ticks
            final long newTick = tick - oldTick > 0L ? tick : oldTick + 1L;
            if (lastTick.compareAndSet(oldTick, newTick)) {
                return newTick;
            }
        }
    }

    void trim() {
        final long now = getTick();
        final long windowStart = now - window;
        final long windowEnd = now + CLEAR_BUFFER;
        if (windowStart < windowEnd) {
            measurements.trim(windowStart, windowEnd);
        } else {
            // long overflow handling that can happen only after 1 year after class loading
            measurements.clear();
        }
    }
}
