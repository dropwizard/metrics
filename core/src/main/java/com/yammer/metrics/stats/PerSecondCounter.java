package com.yammer.metrics.stats;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.yammer.metrics.util.Utils;

public class PerSecondCounter
{
	private static final ScheduledExecutorService TICK_THREAD = Utils.newScheduledThreadPool(2, "per-second-tick");
	private static final long INTERVAL = 1; // seconds
	
	public static PerSecondCounter newPerSecontCounter() {
        final PerSecondCounter counter = new PerSecondCounter();
        final Runnable job = new Runnable() {
            @Override
            public void run() {
            	counter.tick();
            }
        };
        TICK_THREAD.scheduleAtFixedRate(job, INTERVAL, INTERVAL, TimeUnit.SECONDS);
        return counter;
    }
	
	private volatile long rate = 0;
    private final AtomicLong uncounted = new AtomicLong();
    
    private PerSecondCounter(){}

    public void update(long n) {
        uncounted.addAndGet(n);
    }
    
	void tick()
	{
		rate = uncounted.getAndSet(0);
	}
	
	/**
     * Returns the rate in the given units of time.
     *
     * @param rateUnit the unit of time
     * @return the rate
     */
    public long rate() {
        return rate;
    }
}
