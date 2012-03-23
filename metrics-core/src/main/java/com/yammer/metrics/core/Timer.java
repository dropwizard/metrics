package com.yammer.metrics.core;

import com.yammer.metrics.annotation.Publish;
import com.yammer.metrics.core.Histogram.SampleType;
import com.yammer.metrics.stats.Snapshot;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics via {@link Meter}.
 */
public class Timer implements Metered, Stoppable, Sampling, Summarizable {

    private final TimeUnit durationUnit, rateUnit;
    private final Meter meter;
    private final Histogram histogram = new Histogram(SampleType.BIASED);
    private final Clock clock;

    /**
     * Creates a new {@link Timer}.
     *
     * @param tickThread   background thread for updating the rates
     * @param durationUnit the scale unit for this timer's duration metrics
     * @param rateUnit     the scale unit for this timer's rate metrics
     */
    Timer(ScheduledExecutorService tickThread, TimeUnit durationUnit, TimeUnit rateUnit) {
        this(tickThread, durationUnit, rateUnit, Clock.defaultClock());
    }

    /**
     * Creates a new {@link Timer}.
     *
     * @param tickThread   background thread for updating the rates
     * @param durationUnit the scale unit for this timer's duration metrics
     * @param rateUnit     the scale unit for this timer's rate metrics
     * @param clock        the clock used to calculate duration
     */
    Timer(ScheduledExecutorService tickThread, TimeUnit durationUnit, TimeUnit rateUnit, Clock clock) {
        this.durationUnit = durationUnit;
        this.rateUnit = rateUnit;
        this.meter = new Meter(tickThread, "calls", rateUnit, clock);
        this.clock = clock;
        clear();
    }

    /**
     * Returns the timer's duration scale unit.
     *
     * @return the timer's duration scale unit
     */
    public TimeUnit durationUnit() {
        return durationUnit;
    }

    @Override
    public TimeUnit rateUnit() {
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
        final long startTime = clock.tick();
        try {
            return event.call();
        } finally {
            update(clock.tick() - startTime);
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
    @Publish
    public long getCount() {
        return histogram.getCount();
    }

    @Override
    @Publish
    public double getFifteenMinuteRate() {
        return meter.getFifteenMinuteRate();
    }

    @Override
    @Publish
    public double getFiveMinuteRate() {
        return meter.getFiveMinuteRate();
    }

    @Override
    @Publish
    public double getMeanRate() {
        return meter.getMeanRate();
    }

    @Override
    @Publish
    public double getOneMinuteRate() {
        return meter.getOneMinuteRate();
    }

    /**
     * Returns the longest recorded duration.
     *
     * @return the longest recorded duration
     */
    @Override
    @Publish
    public double getMax() {
        return convertFromNS(histogram.getMax());
    }

    /**
     * Returns the shortest recorded duration.
     *
     * @return the shortest recorded duration
     */
    @Override
    @Publish
    public double getMin() {
        return convertFromNS(histogram.getMin());
    }

    /**
     * Returns the arithmetic getMean of all recorded durations.
     *
     * @return the arithmetic getMean of all recorded durations
     */
    @Override
    @Publish
    public double getMean() {
        return convertFromNS(histogram.getMean());
    }

    /**
     * Returns the standard deviation of all recorded durations.
     *
     * @return the standard deviation of all recorded durations
     */
    @Override
    @Publish
    public double getStdDev() {
        return convertFromNS(histogram.getStdDev());
    }

    /**
     * Returns the getSum of all recorded durations.
     *
     * @return the getSum of all recorded durations
     */
    @Override
    @Publish
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

    /**
     * Returns the median value in the distribution.
     *
     * @return the median value in the distribution
     */
    @Publish
    public double getMedian() {
        return getSnapshot().getMedian();
    }

    /**
     * Returns the value at the 75th percentile in the distribution.
     *
     * @return the value at the 75th percentile in the distribution
     */
    @Publish
    public double get75thPercentile() {
        return getSnapshot().get75thPercentile();
    }

    /**
     * Returns the value at the 95th percentile in the distribution.
     *
     * @return the value at the 95th percentile in the distribution
     */
    @Publish
    public double get95thPercentile() {
        return getSnapshot().get95thPercentile();
    }

    /**
     * Returns the value at the 98th percentile in the distribution.
     *
     * @return the value at the 98th percentile in the distribution
     */
    @Publish
    public double get98thPercentile() {
        return getSnapshot().get98thPercentile();
    }

    /**
     * Returns the value at the 99th percentile in the distribution.
     *
     * @return the value at the 99th percentile in the distribution
     */
    @Publish
    public double get99thPercentile() {
        return getSnapshot().get99thPercentile();
    }

    /**
     * Returns the value at the 99.9th percentile in the distribution.
     *
     * @return the value at the 99.9th percentile in the distribution
     */
    @Publish
    public double get999thPercentile() {
        return getSnapshot().get999thPercentile();
    }

    @Override
    public String eventType() {
        return meter.eventType();
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

    @Override
    public void stop() {
        meter.stop();
    }

    @Override
    public <T> void processWith(MetricProcessor<T> processor, MetricName name, T context) throws Exception {
        processor.processTimer(name, this, context);
    }
}
