package com.yammer.metrics.core;

import com.yammer.metrics.util.Utils;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A metric which keeps track of the mean value and count of some event in
 * a configurable rolling window.
 *
 * @author smanek
 */
public class RollingAverageMetric implements Metric {

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

        // we're fine with multiple calls to observe() happening simultaneously, but there are races with
        // mean() and tick() (see their comments), so we need at least a read lock here
        lock.readLock().lock();

        try {
            seconds.getLast().add(val);
            this.total.addAndGet(val);
            this.count.incrementAndGet();
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Returns the number of observations that have been recorded within the window.
     *
     * @return the number of observations that have been recorded within the window.
     */
    public long count() {
        return count.get();
    }


    /**
     * Returns the mean observed value observed within the window.
     *
     * @return the mean observed value observed within the window
     */
    public double mean() {

        // we need a writeLock here (instead of just a read lock to avoid the race with tick())
        // in order to avoid a race with observe() where an observation's value has incremented
        // the window's total, but not yet the window's count
        lock.writeLock().lock();

        try {
            if (count() == 0) {
                return 0.0;
            } else {
                return (double) this.total.get() / count();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private RollingAverageMetric(final long windowSize, final TimeUnit windowUnit) {
        this.windowSize = TimeUnit.SECONDS.convert(windowSize, windowUnit);
        this.total = new AtomicLong(0);
        this.count = new AtomicLong(0);
        this.seconds = new LinkedList<Second>();
        this.last = -1;

        tick();
    }

    private static final ScheduledExecutorService TICK_THREAD = Utils.newScheduledThreadPool(2, "rolling-tick");
    private static final long INTERVAL = 1; // seconds

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

    // list of seconds for which we have data
    private final Deque<Second> seconds;

    // number of seconds in the window
    private final long windowSize;

    // the last timestamp we have data for
    private long last;

    // the total value in the window
    private final AtomicLong total;

    // the total count in the window
    private final AtomicLong count;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();


    private void tick() {

        // this writeLock is needed for three reasons.
        // 1. To prevent two tick()s from running simultaneously (which will break many things, not least modifying
        // the LinkedList 'seconds' concurrently).
        // 2. To prevent the mean() method from being called at the same time as this - which could result in
        // a race where we compute the mean while one second's total has been removed from the window's total,
        // but that second's count hasn't been removed from the window's count
        // 3. To prevent the observe method from running at the same time as this - which (with very small windows)
        // could result in a race where a second is removed from the seconds list while only its count has been
        // incremented (without the corresponding total increment).        
        lock.writeLock().lock();
        try {
            final long now = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

            if (now > last) {
                seconds.addLast(new Second(now));

                final long earliestAllowedTimeStamp = now - windowSize;
                Iterator<Second> iterator = seconds.iterator();
                while (iterator.hasNext()) {
                    Second s = iterator.next();
                    if (s.getTimestamp() < earliestAllowedTimeStamp) {
                        iterator.remove();
                        total.addAndGet(0 - s.getTotal().get());
                        count.addAndGet(0 - s.getCount().get());
                    } else {
                        break;
                    }
                }

                last = now;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
