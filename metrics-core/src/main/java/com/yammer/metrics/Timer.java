package com.yammer.metrics;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics via {@link Meter}.
 */
public class Timer implements Metered, Sampling, Summarizable {
    private final Meter meter;
    private final Histogram histogram = new Histogram(SampleType.BIASED);
    private final Clock clock;

    /**
     * Creates a new {@link Timer}.
     */
    public Timer() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link Timer}.
     *
     * @param clock        the clock used to calculate duration
     */
    Timer(Clock clock) {
        this.meter = new Meter(clock);
        this.clock = clock;
        clear();
    }

    /**
     * Clears all recorded durations.
     */
    public void clear() {
        histogram.clear();
    }

    /**
     * Adds a recorded duration.
     *
     * @param duration the length of the duration
     * @param unit     the scale unit of {@code duration}
     */
    public void update(long duration, TimeUnit unit) {
        update(unit.toNanos(duration));
    }

    /**
     * Times and records the duration of event.
     *
     * @param event a {@link Callable} whose {@link Callable#call()} method implements a process
     *              whose duration should be timed
     * @param <T>   the type of the value returned by {@code event}
     * @return the value returned by {@code event}
     * @throws Exception if {@code event} throws an {@link Exception}
     */
    public <T> T time(Callable<T> event) throws Exception {
        final long startTime = clock.getTick();
        try {
            return event.call();
        } finally {
            update(clock.getTick() - startTime);
        }
    }

    /**
     * Returns a timing {@link Context}, which measures an elapsed time in nanoseconds.
     *
     * @return a new {@link Context}
     * @see Context
     */
    public Context time() {
        return new Context(this, clock);
    }

    @Override
    public long getCount() {
        return histogram.getCount();
    }

    @Override
    public double getFifteenMinuteRate() {
        return meter.getFifteenMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return meter.getFiveMinuteRate();
    }

    @Override
    public double getMeanRate() {
        return meter.getMeanRate();
    }

    @Override
    public double getOneMinuteRate() {
        return meter.getOneMinuteRate();
    }

    /**
     * Returns the longest recorded duration.
     *
     * @return the longest recorded duration
     */
    @Override
    public long getMax() {
        return histogram.getMax();
    }

    /**
     * Returns the shortest recorded duration.
     *
     * @return the shortest recorded duration
     */
    @Override
    public long getMin() {
        return histogram.getMin();
    }

    /**
     * Returns the arithmetic mean of all recorded durations.
     *
     * @return the arithmetic mean of all recorded durations
     */
    @Override
    public double getMean() {
        return histogram.getMean();
    }

    /**
     * Returns the standard deviation of all recorded durations.
     *
     * @return the standard deviation of all recorded durations
     */
    @Override
    public double getStdDev() {
        return histogram.getStdDev();
    }

    /**
     * Returns the sum of all recorded durations.
     *
     * @return the sum of all recorded durations
     */
    @Override
    public long getSum() {
        return histogram.getSum();
    }

    @Override
    public Snapshot getSnapshot() {
        return new Snapshot(histogram.getSnapshot().getValues());
    }

    private void update(long duration) {
        if (duration >= 0) {
            histogram.update(duration);
            meter.mark();
        }
    }

    /**
     * A timing context.
     *
     * @see Timer#time()
     */
    public static class Context {
        private final Timer timer;
        private final Clock clock;
        private final long startTime;

        private Context(Timer timer, Clock clock) {
            this.timer = timer;
            this.clock = clock;
            this.startTime = clock.getTick();
        }

        /**
         * Stops recording the elapsed time, updates the timer and returns the elapsed time in
         * nanoseconds.
         */
        public long stop() {
            final long elapsedNanos = clock.getTick() - startTime;
            timer.update(elapsedNanos, TimeUnit.NANOSECONDS);
            return elapsedNanos;
        }
    }
}
