package com.yammer.metrics.core;

import com.yammer.metrics.util.Utils;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A metric which keeps track of the mean value and count of some event in
 * a configurable rolling window.
 *
 * @author smanek
 */
public class RollingAverageMetric implements Metric {

    private static final ScheduledExecutorService TICK_THREAD = Utils.newScheduledThreadPool(2, "rolling-tick");
    private static final long INTERVAL = 1; // seconds

    // list of seconds for which we have data
    private final Deque<Second> secondsInWindow;

    // the current second we're collecting data in
    private final AtomicReference<Second> currentSecond;

    // number of seconds in the window
    private final long windowSize;

    // the total value in the window
    private final AtomicLong totalInWindow;

    // the total count in the window
    private final AtomicLong countInWindow;

    /**
     * Creates a new {@link RollingAverageMetric}.
     *
     * @param windowSize the size of the window statistics should be kept for
     * @param windowUnit the unit the window size is given in
     * @return a new {@link RollingAverageMetric}
     */
    public static RollingAverageMetric newRollingAverage(long windowSize, TimeUnit windowUnit) {
        final RollingAverageMetric meter = new RollingAverageMetric(windowSize, windowUnit);
        final Runnable job = new Runnable() {
            public void run() {
                meter.tick();
            }
        };
        TICK_THREAD.scheduleAtFixedRate(job, INTERVAL, INTERVAL, TimeUnit.SECONDS);
        return meter;
    }


    /**
     * Observe the occurrence of a particular value of an event
     *
     * @param val the value of the event
     */
    public void observe(long val) {
        currentSecond.get().add(val);
    }


    /**
     * Returns the number of observations that have been recorded within the window.
     *
     * @return the number of observations that have been recorded within the window.
     */
    public long count() {
        return countInWindow.get();
    }


    /**
     * Returns the mean observed value observed within the window.
     *
     * @return the mean observed value observed within the window
     */
    public synchronized double mean() {
        // synchronized with tick() to prevent getting an updated total but a not-updated count
        if (count() == 0) {
            return 0.0;
        } else {
            return (double) this.totalInWindow.get() / count();
        }
    }

    private RollingAverageMetric(final long windowSize, final TimeUnit windowUnit) {
        this.windowSize = TimeUnit.SECONDS.convert(windowSize, windowUnit);

        this.totalInWindow = new AtomicLong(0);
        this.countInWindow = new AtomicLong(0);
        this.secondsInWindow = new LinkedList<Second>();

        this.currentSecond = new AtomicReference<Second>(new Second(now()));
    }

    /**
     * Inner class representing one second's worth of data.
     */
    private static class Second {
        private final long timestamp;

        private final AtomicLong count;
        private final AtomicLong total;

        public Second(long timestamp) {
            this.timestamp = timestamp;
            count = new AtomicLong(0);
            total = new AtomicLong(0);
        }

        public void add(long value) {
            count.incrementAndGet();
            total.addAndGet(value);
        }

        public long getTimestamp() {
            return timestamp;
        }

        public AtomicLong getCount() {
            return count;
        }

        public AtomicLong getTotal() {
            return total;
        }
    }

    private static long now() {
        return TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    private synchronized void tick() {
        // synchronized to avoid two ticks running at once (which could concurrently modify
        // secondsInWindow), and to prevent mean() from seeing inconsistent aggregates
        final long now = now();
        if (now > currentSecond.get().getTimestamp()) {
            final long earliestAllowedTimeStamp = now - windowSize;
            final Second processing = currentSecond.getAndSet(new Second(now));
            totalInWindow.addAndGet(processing.getTotal().get());
            countInWindow.addAndGet(processing.getCount().get());
            secondsInWindow.addLast(processing);

            Iterator<Second> iterator = secondsInWindow.iterator();
            while (iterator.hasNext()) {
                Second s = iterator.next();
                if (s.getTimestamp() < earliestAllowedTimeStamp) {
                    iterator.remove();
                    totalInWindow.addAndGet(0 - s.getTotal().get());
                    countInWindow.addAndGet(0 - s.getCount().get());
                } else {
                    break;
                }
            }
        }
    }
}
