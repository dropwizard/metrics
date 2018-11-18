package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ExponentialMovingAverages implements MovingAverages {

    private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    private final EWMA m1Rate = EWMA.oneMinuteEWMA();
    private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
    private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

    private final long startTime;
    private final AtomicLong lastTick;
    private final Clock clock;

    public ExponentialMovingAverages() {
        this(Clock.defaultClock());
    }

    public ExponentialMovingAverages(Clock clock) {
        this.clock = clock;
        this.startTime = this.clock.getTick();
        this.lastTick = new AtomicLong(startTime);
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
                for (long i = 0; i < requiredTicks; i++) {
                    m1Rate.tick();
                    m5Rate.tick();
                    m15Rate.tick();
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
