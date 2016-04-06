package io.dropwizard.metrics;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Reservoir} implementation backed by a sliding window that stores only the measurements made
 * in the last {@code N} seconds (or other time unit).
 */
public class SlidingTimeWindowReservoir implements Reservoir {

    // allow for this many duplicate ticks before overwriting measurements
    private final int collisionBuffer;
    private final long collisionModulo;

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
        this(window, windowUnit, clock, 8);
    }

    /**
     * Creates a new {@link SlidingTimeWindowReservoir} with the given window of time and collisionBitwidth.
     * 
     * @param window            the window of time
     * @param windowUnit        the unit of {@code window}
     * @param collisionBitwidth the bit size of the collision buffer (default is 8 which will yield 256 observations per nanosecond)
     */
    public SlidingTimeWindowReservoir(long window, TimeUnit windowUnit, int collisionBitwidth) {
        this(window, windowUnit, Clock.defaultClock(), collisionBitwidth);
    }

    /**
     * Creates a new {@link SlidingTimeWindowReservoir} with the given clock, window of time and collisionBitwidth.
     * 
     * @param window            the window of time
     * @param windowUnit        the unit of {@code window}
     * @param clock             the {@link Clock} to use
     * @param collisionBitwidth the bit size of the collision buffer (default is 8 which will yield 256 observations per nanosecond)
     */
    public SlidingTimeWindowReservoir(long window, TimeUnit windowUnit, Clock clock, int collisionBitwidth) {
        this.collisionBuffer = 1 << collisionBitwidth;
        this.collisionModulo = (1L << (63 - collisionBitwidth)) - 1;
        this.clock = clock;
        this.measurements = new ConcurrentSkipListMap<Long, Long>();
        this.window = windowUnit.toNanos(window) * collisionBuffer;
        this.lastTick = new AtomicLong((clock.getTick() & collisionModulo) * collisionBuffer);
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
            final long tick = (clock.getTick() & collisionModulo) * collisionBuffer;
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
