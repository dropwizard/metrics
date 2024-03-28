package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A triple (one, five and fifteen minutes) of exponentially-weighted moving average rates as needed by {@link Meter}.
 * <p>
 * The rates have the same exponential decay factor as the fifteen-minute load average in the
 * {@code top} Unix command.
 */
public class ExponentialMovingAverages implements MovingAverages {

    /**
     * If ticking would reduce even Long.MAX_VALUE in the 15 minute EWMA below this target then don't bother
     * ticking in a loop and instead reset all the EWMAs.
     */
    private static final double maxTickZeroTarget = 0.0001;
    private static final int maxTicks;
    private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    static
    {
        int m3Ticks = 1;
        final EWMA m3 = EWMA.fifteenMinuteEWMA();
        m3.update(Long.MAX_VALUE);
        do
        {
            m3.tick();
            m3Ticks++;
        }
        while (m3.getRate(TimeUnit.SECONDS) > maxTickZeroTarget);
        maxTicks = m3Ticks;
    }

    private final EWMA m1Rate = EWMA.oneMinuteEWMA();
    private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
    private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

    private final AtomicLong lastTick;
    private final Clock clock;

    /**
     * Creates a new {@link ExponentialMovingAverages}.
     */
    public ExponentialMovingAverages() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link ExponentialMovingAverages}.
     */
    public ExponentialMovingAverages(Clock clock) {
        this.clock = clock;
        this.lastTick = new AtomicLong(this.clock.getTick());
    }

    @Override
    public void update(long n) {
        m1Rate.update(n);
        m5Rate.update(n);
        m15Rate.update(n);
    }

    @Override
    public void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age > TICK_INTERVAL) {
            final long newIntervalStartTick = newTick - age % TICK_INTERVAL;
            if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
                final long requiredTicks = age / TICK_INTERVAL;
                if (requiredTicks >= maxTicks) {
                    m1Rate.reset();
                    m5Rate.reset();
                    m15Rate.reset();
                }
                else
                {
                    for (long i = 0; i < requiredTicks; i++)
                    {
                        m1Rate.tick();
                        m5Rate.tick();
                        m15Rate.tick();
                    }
                }
            }
        }
    }

    @Override
    public double getM1Rate() {
        return m1Rate.getRate(TimeUnit.SECONDS);
    }

    @Override
    public double getM5Rate() {
        return m5Rate.getRate(TimeUnit.SECONDS);
    }

    @Override
    public double getM15Rate() {
        return m15Rate.getRate(TimeUnit.SECONDS);
    }
}
