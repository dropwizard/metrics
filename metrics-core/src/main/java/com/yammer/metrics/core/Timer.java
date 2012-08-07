package com.yammer.metrics.core;

import com.yammer.metrics.core.Histogram.SampleType;
import com.yammer.metrics.stats.Snapshot;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics via {@link Meter}.
 */
public class Timer implements Metered, Sampling, Summarizable {
    private final TimeUnit durationUnit, rateUnit;
    private final Meter meter;
    private final Histogram histogram = new Histogram(SampleType.BIASED);
    private final Clock clock;

    /**
     * Creates a new {@link Timer}.
     *
     * @param durationUnit the scale unit for this timer's duration metrics
     * @param rateUnit     the scale unit for this timer's rate metrics
     * @param clock        the clock used to calculate duration
     */
    Timer(TimeUnit durationUnit, TimeUnit rateUnit, Clock clock) {
        this.durationUnit = durationUnit;
        this.rateUnit = rateUnit;
        this.meter = new Meter("calls", rateUnit, clock);
        this.clock = clock;
        clear();
    }

    /**
     * Returns the timer's duration scale unit.
     *
     * @return the timer's duration scale unit
     */
    public TimeUnit getDurationUnit() {
        return durationUnit;
    }

    @Override
    public TimeUnit getRateUnit() {
        return rateUnit;
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
     * Returns a timing {@link TimerContext}, which measures an elapsed time in nanoseconds.
     *
     * @return a new {@link TimerContext}
     */
    public TimerContext time() {
        return new TimerContext(this, clock);
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
    public double getMax() {
        return convertFromNS(histogram.getMax());
    }

    /**
     * Returns the shortest recorded duration.
     *
     * @return the shortest recorded duration
     */
    @Override
    public double getMin() {
        return convertFromNS(histogram.getMin());
    }

    /**
     * Returns the arithmetic mean of all recorded durations.
     *
     * @return the arithmetic mean of all recorded durations
     */
    @Override
    public double getMean() {
        return convertFromNS(histogram.getMean());
    }

    /**
     * Returns the standard deviation of all recorded durations.
     *
     * @return the standard deviation of all recorded durations
     */
    @Override
    public double getStdDev() {
        return convertFromNS(histogram.getStdDev());
    }

    /**
     * Returns the sum of all recorded durations.
     *
     * @return the sum of all recorded durations
     */
    @Override
    public double getSum() {
        return convertFromNS(histogram.getSum());
    }

    @Override
    public Snapshot getSnapshot() {
        final double[] values = histogram.getSnapshot().getValues();
        final double[] converted = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            converted[i] = convertFromNS(values[i]);
        }
        return new Snapshot(converted);
    }

    @Override
    public String getEventType() {
        return meter.getEventType();
    }

    private void update(long duration) {
        if (duration >= 0) {
            histogram.update(duration);
            meter.mark();
        }
    }

    private double convertFromNS(double ns) {
        return ns / TimeUnit.NANOSECONDS.convert(1, durationUnit);
    }
}
