package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Reservoir} implementation backed by a sliding window that stores only the measurements made
 * in the last {@code N} seconds (or other time unit).
 */
public class SlidingTimeWindowChankedArrayBasedReservoir implements Reservoir {
    // allow for this many duplicate ticks before overwriting measurements
    private static final int COLLISION_BUFFER = 256;
    // only trim on updating once every N
    private static final int TRIM_THRESHOLD = 256;
    private static final long CLEAR_BUFFER = TimeUnit.HOURS.toNanos(1) * COLLISION_BUFFER;

    private final Clock clock;
    final ChunkedAssociativeLongArray measurements;
    private final long window;
    final AtomicLong lastTick;
    private final AtomicLong count;

    /**
     * Creates a new {@link SlidingTimeWindowChankedArrayBasedReservoir} with the given window of time.
     *
     * @param window     the window of time
     * @param windowUnit the unit of {@code window}
     */
    public SlidingTimeWindowChankedArrayBasedReservoir(long window, TimeUnit windowUnit) {
        this(window, windowUnit, Clock.defaultClock());
    }

    /**
     * Creates a new {@link SlidingTimeWindowChankedArrayBasedReservoir} with the given clock and window of time.
     *
     * @param window     the window of time
     * @param windowUnit the unit of {@code window}
     * @param clock      the {@link Clock} to use
     */
    public SlidingTimeWindowChankedArrayBasedReservoir(long window, TimeUnit windowUnit, Clock clock) {
        this.clock = clock;
        this.measurements = new ChunkedAssociativeLongArray(512);
        this.window = windowUnit.toNanos(window) * COLLISION_BUFFER;
        this.lastTick = new AtomicLong(clock.getTick() * COLLISION_BUFFER);
        this.count = new AtomicLong();
    }

    @Override
    public int size() {
        trim();
        final long now = lastTick.get();
        final long windowStart = now - window;
        return measurements.size(windowStart);
    }

    @Override
    public void update(long value) {
        if (count.incrementAndGet() % TRIM_THRESHOLD == 0) {
            trim();
        }
        long lastTick = this.lastTick.get();
        long newTick = getTick();
        boolean longOverflow = newTick < lastTick;
        if (longOverflow) {
            measurements.clear();
        }
        measurements.put(newTick, value);
    }

    @Override
    public Snapshot getSnapshot() {
        trim();
        final long now = lastTick.get();
        final long windowStart = now - window;
        return new UniformSnapshot(measurements.values(windowStart));
    }

    private long getTick() {
        for (; ; ) {
            final long oldTick = lastTick.get();
            final long tick = clock.getTick() * COLLISION_BUFFER;
            // ensure the tick is strictly incrementing even if there are duplicate ticks
            final long newTick = tick - oldTick > 0 ? tick : oldTick + 1;
            if (lastTick.compareAndSet(oldTick, newTick)) {
//                System.out.println("arr tick: " + newTick);
                return newTick;
            }
        }
    }

    private void trim() {
        final long now = getTick();
        final long windowStart = now - window;
        final long windowEnd = now + CLEAR_BUFFER;
//        System.out.println("arr");
//        System.out.println(windowStart);
//        System.out.println(windowEnd);
        if (windowStart < windowEnd) {
            measurements.trim(windowStart, windowEnd);
        } else {
            measurements.clear(windowEnd, windowStart);
        }
    }
}
