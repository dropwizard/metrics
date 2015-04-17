package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A meter metric which measures mean count and one-, five-, and fifteen-minute
 * exponentially-weighted moving average counts.
 *
 * @see EWMA
 * @see Counter
 */
public class CounterMeter extends Meter implements Metered, Counting {
	private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    private final EWMA m1Rate = EWMA.oneMinuteEWMA();
    private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
    private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();
    
    private final LongAdder counter = new LongAdder();
    private final long startTime;
    private final AtomicLong lastTick;
    private final Clock clock;
    
    /**
     * Creates a new {@link CounterMeter}.
     */
    public CounterMeter() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link CounterMeter}.
     *
     * @param clock      the clock to use for the meter ticks
     */
    public CounterMeter(Clock clock) {
        this.clock = clock;
        this.startTime = this.clock.getTick();
        this.lastTick = new AtomicLong(startTime);
    }

    private void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age > TICK_INTERVAL) {
        	final long currentCount = getCount();
            m1Rate.update(currentCount);
            m5Rate.update(currentCount);
            m15Rate.update(currentCount);
            
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
    public void mark() {
        mark(1);
    }


    @Override
    public void mark(long n) {
        tickIfNecessary();
        inc(n);
    }
    
    public void inc() {
        inc(1);
    }

    public void dec() {
    	dec(1);
    }
    
    public void inc(long n) {
    	counter.add(n);
        tickIfNecessary();
    }

    public void dec(long n) {
    	counter.add(-n);
        tickIfNecessary();
    }
    
    @Override
    public long getCount() {
        return counter.sum();
    }

    @Override
    public double getFifteenMinuteRate() {
        tickIfNecessary();
        return m15Rate.getRate(TimeUnit.SECONDS);
    }

    @Override
    public double getFiveMinuteRate() {
        tickIfNecessary();
        return m5Rate.getRate(TimeUnit.SECONDS);
    }

    @Override
    public double getMeanRate() {
        if (getCount() == 0) {
            return 0.0;
        } else {
            final double elapsed = (clock.getTick() - startTime);
            return getCount() / elapsed * TimeUnit.SECONDS.toNanos(1);
        }
    }

    @Override
    public double getOneMinuteRate() {
        tickIfNecessary();
        return m1Rate.getRate(TimeUnit.SECONDS);
    }
}
