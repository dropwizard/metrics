package com.yammer.metrics.core;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.stats.Snapshot;

/**
 * A timeout specified by a threshold, a unit ({@link TimeUnit}) and coupled with an action
 * ({@link Timeout#ifExceeded()}) to take if the timeout is exceeded.
 */
public abstract class Timeout extends TimerTask {

    private final TimeUnit unit;
    private final long threshold;

    /**
     * @param threshold The threshold value
     * @param unit The unit of the threshold value
     */
    public Timeout(long threshold, TimeUnit unit) {
        this.threshold = threshold;
        this.unit = unit;
    }

    /**
     * Schedule this {@link Timeout} with the given {@link Timer}.
     * 
     * @param timer The timer to schedule this {@link Timeout} to
     * @param ignored the {@link TimerMetric} scheduling this {@link Timeout}
     */
    public void scheduleTo(java.util.Timer timer, Timer ignored) {
        timer.schedule(this, unit.toMillis(threshold));
    }

    /**
     * Called if the timeout is exceeded.
     */
    protected abstract void ifExceeded();

    /*
     * (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
    @Override
    public final void run() {
        ifExceeded();
    }

    /**
     * {@link QuantileTimeout} is the instance that gets scheduled to the {@link Timer} whereupon it
     * actually schedules the {@link Timeout} returned by {@link QuantileTimeout#actual}.
     */
    public static abstract class QuantileTimeout extends Timeout {

        private final Object mutex = new Object();
        private Timeout actual = null;
        private final double quantile;

        /**
         * Creates a new {@link QuantileTimeout} instance that will invoke its {@link Timeout#ifExceeded()}
         * method if the timed task exceeds the given quantile.
         * 
         * @param quantile
         * 
         * @see Snapshot#MEDIAN_Q
         * @see Snapshot#P75_Q
         * @see Snapshot#P95_Q
         * @see Snapshot#P98_Q
         * @see Snapshot#P99_Q
         * @see Snapshot#P999_Q
         */
        public QuantileTimeout(final double quantile) {
            // These values can be whatever since this instance is never scheduled
            super(0, TimeUnit.MICROSECONDS);
            this.quantile = quantile;
        }

        /**
         * Returns the {@link Timeout} instance that'll actually be scheduled with the {@link Timer} when
         * {@link Timeout#scheduleTo(Timer, TimerMetric)} is called on this instance.
         * 
         * @param metric The {@link TimerMetric} instance
         * @return the {@link Timeout} instance that will be scheduled 
         */
        public final Timeout actual(Timer metric) {
            final Timeout parent = this;
            return new Timeout(Math.round(metric.getSnapshot().getValue(quantile)), metric.durationUnit()) {
                @Override
                protected void ifExceeded() {
                    // Call the ifExceeded method of our enclosing class' parent
                    // since that's where the magic happens
                    parent.ifExceeded();
                }
            };
        }

        @Override
        public final void scheduleTo(java.util.Timer timer, Timer metric) {
            // Instead of scheduling ourselves, we schedule the return
            // value from actual() which we also store so that we're
            // able to cancel it later, instead of cancelling our
            // selves since that won't make anyone happy (we were
            // never scheduled in the first place)
            synchronized(this.mutex) {
                if(this.actual == null) {
                    this.actual = actual(metric);
                    this.actual.scheduleTo(timer, metric);
                }
            }
        }

        @Override
        public final boolean cancel() {
            // If scheduleTo has been invoked that means we've scheduled
            // the value of this.actual hence that's what we cancel
            synchronized(this.mutex) {
                if(this.actual != null) {
                    final boolean result = this.actual.cancel();
                    this.actual = null;
                    return result;
                }
            }
            return false;
        }
    }
}