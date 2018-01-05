package com.codahale.metrics;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics via {@link Meter}.
 */
public class Timer implements Metered, Sampling {

    /**
     * A timing context.
     *
     * @see Timer#time()
     */
    public interface Context extends AutoCloseable {

        /**
         * Updates the timer with the difference between current and start time. Call to this method will
         * not reset the start time. Multiple calls result in multiple updates.
         *
         * @return the elapsed time in nanoseconds
         */
        long stop();

        /**
         * Equivalent to calling {@link #stop()}.
         */
        @Override
        default void close() {
            stop();
        }
    }

    /**
     * The default timing context.
     */
    private static class DefaultContext implements Context {

        private final Timer timer;
        private final Clock clock;
        private final long startTime;

        private DefaultContext(Timer timer, Clock clock) {
            this.timer = timer;
            this.clock = clock;
            this.startTime = clock.getTick();
        }

        @Override
        public long stop() {
            final long elapsed = clock.getTick() - startTime;
            timer.update(elapsed, TimeUnit.NANOSECONDS);
            return elapsed;
        }
    }

    private final Meter meter;
    private final Histogram histogram;
    private final Clock clock;

    /**
     * Creates a new {@link Timer} using an {@link ExponentiallyDecayingReservoir} and the default
     * {@link Clock}.
     */
    public Timer() {
        this(new ExponentiallyDecayingReservoir());
    }

    /**
     * Creates a new {@link Timer} that uses the given {@link Reservoir}.
     *
     * @param reservoir the {@link Reservoir} implementation the timer should use
     */
    public Timer(Reservoir reservoir) {
        this(reservoir, Clock.defaultClock());
    }

    /**
     * Creates a new {@link Timer} that uses the given {@link Reservoir} and {@link Clock}.
     *
     * @param reservoir the {@link Reservoir} implementation the timer should use
     * @param clock     the {@link Clock} implementation the timer should use
     */
    public Timer(Reservoir reservoir, Clock clock) {
        this.meter = new Meter(clock);
        this.clock = clock;
        this.histogram = new Histogram(reservoir);
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
     * Times and records the duration of event. Should not throw exceptions, for that use the
     * {@link #time(Callable)} method.
     *
     * @param event a {@link Supplier} whose {@link Supplier#get()} method implements a process
     *              whose duration should be timed
     * @param <T>   the type of the value returned by {@code event}
     * @return the value returned by {@code event}
     */
    public <T> T timeSupplier(Supplier<T> event) {
        final long startTime = clock.getTick();
        try {
            return event.get();
        } finally {
            update(clock.getTick() - startTime);
        }
    }

    /**
     * Times and records the duration of event.
     *
     * @param event a {@link Runnable} whose {@link Runnable#run()} method implements a process
     *              whose duration should be timed
     */
    public void time(Runnable event) {
        final long startTime = clock.getTick();
        try {
            event.run();
        } finally {
            update(clock.getTick() - startTime);
        }
    }

    /**
     * Returns a new {@link Context}.
     *
     * @return a new {@link Context}
     * @see Context
     */
    public Context time() {
        return new DefaultContext(this, clock);
    }

    @Override
    public long getCount() {
        return histogram.getCount();
    }

    @Override
    public long getSum() {
        return histogram.getSum();
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

    @Override
    public Snapshot getSnapshot() {
        return histogram.getSnapshot();
    }

    private void update(long duration) {
        if (duration >= 0) {
            histogram.update(duration);
            meter.mark();
        }
    }
}
