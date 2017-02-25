package com.codahale.metrics;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MeasurementPublisher.DO_NOT_PUBLISH;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics via {@link Meter}.
 */
public class Timer implements Metered, Sampling {

    private static final TimeUnit TIMER_TIME_UNIT = TimeUnit.NANOSECONDS;

    /**
     * A timing context.
     *
     * @see Timer#time()
     */
    public static class Context implements Closeable {
        private final Timer timer;
        private final Clock clock;
        private final long startTime;

        private Context(Timer timer, Clock clock) {
            this.timer = timer;
            this.clock = clock;
            this.startTime = clock.getTick();
        }

        /**
         * Updates the timer with the difference between current and start time. Call to this method will
         * not reset the start time. Multiple calls result in multiple updates.
         * @return the elapsed time in nanoseconds
         */
        public long stop() {
            final long elapsed = clock.getTick() - startTime;
            timer.update(elapsed, TimeUnit.NANOSECONDS);
            return elapsed;
        }

        /** Equivalent to calling {@link #stop()}. */
        @Override
        public void close() {
            stop();
        }
    }

    private final String name;
    private final MeasurementPublisher measurementPublisher;
    private final Meter meter;
    private final Histogram histogram;
    private final Clock clock;

    /**
     * Creates a new {@link Timer} using an {@link ExponentiallyDecayingReservoir} and the default
     * {@link Clock}.
     * @param name The name of the metric
     * @param measurementPublisher a publisher which the metric should notify of any measurements
     */
    public Timer(String name, MeasurementPublisher measurementPublisher) {
        this(name, measurementPublisher, new ExponentiallyDecayingReservoir());
    }

    /**
     * Creates a new {@link Timer} that uses the given {@link Reservoir}.
     *
     * @param name The name of the metric
     * @param measurementPublisher a publisher which the metric should notify of any measurements
     * @param reservoir the {@link Reservoir} implementation the timer should use
     */
    public Timer(String name, MeasurementPublisher measurementPublisher, Reservoir reservoir) {
        this(name, measurementPublisher, reservoir, Clock.defaultClock());
    }

    /**
     * Creates a new {@link Timer} that uses the given {@link Reservoir} and {@link Clock}.
     *
     * @param name The name of the metric
     * @param measurementPublisher a publisher which the metric should notify of any measurements
     * @param reservoir the {@link Reservoir} implementation the timer should use
     * @param clock  the {@link Clock} implementation the timer should use
     */
    public Timer(String name, MeasurementPublisher measurementPublisher, Reservoir reservoir, Clock clock) {
        this.name = name;
        this.measurementPublisher = measurementPublisher;
        this.meter = new Meter(name, DO_NOT_PUBLISH, clock);
        this.clock = clock;
        this.histogram = new Histogram(name, DO_NOT_PUBLISH, reservoir);
    }

    /**
     * Adds a recorded duration.
     *
     * @param duration the length of the duration
     * @param unit     the scale unit of {@code duration}
     */
    public void update(long duration, TimeUnit unit) {
        update(TIMER_TIME_UNIT.convert(duration, unit));
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

    @Override
    public Snapshot getSnapshot() {
        return histogram.getSnapshot();
    }

    private void update(long duration) {
        if (duration >= 0) {
            histogram.update(duration);
            meter.mark();
        }
        measurementPublisher.timerUpdated(name, duration, TIMER_TIME_UNIT);
    }
}
