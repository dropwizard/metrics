package com.yammer.metrics.core;

import com.yammer.metrics.stats.EWMA;
import com.yammer.metrics.util.Utils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A meter metric which measures mean throughput and one-, five-, and
 * fifteen-minute exponentially-weighted moving average throughputs.
 *
 * @author coda
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
public class MeterMetric implements Metered {
    private static final ScheduledExecutorService TICK_THREAD = Utils.newScheduledThreadPool(2, "meter-tick");
    private static final long INTERVAL = 5; // seconds

    /**
     * Creates a new {@link MeterMetric}.
     *
     * @param eventType the plural name of the event the meter is measuring
     *                  (e.g., {@code "requests"})
     * @param rateUnit the rate unit of the new meter
     * @return a new {@link MeterMetric}
     */
    public static MeterMetric newMeter(String eventType, TimeUnit rateUnit) {
        final MeterMetric meter = new MeterMetric(eventType, rateUnit);
        final Runnable job = new Runnable() {
            @Override
            public void run() {
                meter.tick();
            }
        };
        TICK_THREAD.scheduleAtFixedRate(job, INTERVAL, INTERVAL, TimeUnit.SECONDS);
        return meter;
    }

    private final EWMA m1Rate = EWMA.oneMinuteEWMA();
    private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
    private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

    private final AtomicLong count = new AtomicLong();
    private final long startTime = System.nanoTime();
    private final TimeUnit rateUnit;
    private final String eventType;

    private MeterMetric(String eventType, TimeUnit rateUnit) {
        this.rateUnit = rateUnit;
        this.eventType = eventType;
    }

    @Override
    public TimeUnit rateUnit() {
        return rateUnit;
    }

    @Override
    public String eventType() {
        return eventType;
    }

    /**
     * Updates the moving averages.
     */
    void tick() {
        m1Rate.tick();
        m5Rate.tick();
        m15Rate.tick();
    }

    /**
     * Mark the occurrence of an event.
     */
    public void mark() {
        mark(1);
    }

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public void mark(long n) {
        count.addAndGet(n);
        m1Rate.update(n);
        m5Rate.update(n);
        m15Rate.update(n);
    }

    @Override
    public long count() {
        return count.get();
    }

    @Override
    public double fifteenMinuteRate() {
        return m15Rate.rate(rateUnit);
    }

    @Override
    public double fiveMinuteRate() {
        return m5Rate.rate(rateUnit);
    }

    @Override
    public double meanRate() {
        if (count() == 0) {
            return 0.0;
        } else {
            final long elapsed = (System.nanoTime() - startTime);
            return convertNsRate(count() / (double) elapsed);
        }
    }

    @Override
    public double oneMinuteRate() {
        return m1Rate.rate(rateUnit);
    }

    private double convertNsRate(double ratePerNs) {
        return ratePerNs * (double) rateUnit.toNanos(1);
    }
}
