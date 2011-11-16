package com.yammer.metrics.core;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.stats.EWMA;
import com.yammer.metrics.util.Utils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A meter metric which measures mean throughput and one-, five-, and
 * fifteen-minute exponentially-weighted moving average throughputs.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
public class MeterMetric implements Metered, Stoppable {
    private static final long INTERVAL = 5; // seconds

    /**
     * Creates a new {@link MeterMetric}.
     *
     * @param eventType the plural name of the event the meter is measuring
     *                  (e.g., {@code "requests"})
     * @param rateUnit the rate unit of the new meter
     * @return a new {@link MeterMetric}
     * @deprecated use the other {@code newMeter} method or create a new meter via the
     *              {@link MetricsRegistry} or {@link Metrics}
     */
    @SuppressWarnings({"deprecation"})
    public static MeterMetric newMeter(String eventType, TimeUnit rateUnit) {
        return newMeter(Utils.newScheduledThreadPool(2, "meter-tick"), eventType, rateUnit);
    }

    /**
     * Creates a new {@link MeterMetric}.
     *
     * @param tickThread background thread for updating the rates
     * @param eventType the plural name of the event the meter is measuring
     *                  (e.g., {@code "requests"})
     * @param rateUnit the rate unit of the new meter
     * @return a new {@link MeterMetric}
     */
    public static MeterMetric newMeter(ScheduledExecutorService tickThread, String eventType, TimeUnit rateUnit) {
        return new MeterMetric(tickThread, eventType, rateUnit);
    }

    private final EWMA m1Rate = EWMA.oneMinuteEWMA();
    private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
    private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

    private final AtomicLong count = new AtomicLong();
    private final long startTime = System.nanoTime();
    private final TimeUnit rateUnit;
    private final String eventType;
    private final ScheduledFuture<?> future;

    private MeterMetric(ScheduledExecutorService tickThread, String eventType, TimeUnit rateUnit) {
        this.rateUnit = rateUnit;
        this.eventType = eventType;
        this.future = tickThread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, INTERVAL, INTERVAL, TimeUnit.SECONDS);
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

    @Override
    public void stop() {
        future.cancel(false);
    }
}
