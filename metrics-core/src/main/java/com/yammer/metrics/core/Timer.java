package com.yammer.metrics.core;

import com.yammer.metrics.core.Histogram.SampleType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics via {@link Meter}.
 */
public class Timer implements Metered, Stoppable, Quantized, Summarized {

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
        this(tickThread, durationUnit, rateUnit, Clock.DEFAULT);
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
        return new TimerContext(this);
    }

    @Override
    public long count() {
        return histogram.count();
    }

    @Override
    public double fifteenMinuteRate() {
        return meter.fifteenMinuteRate();
    }

    @Override
    public double fiveMinuteRate() {
        return meter.fiveMinuteRate();
    }

    @Override
    public double meanRate() {
        return meter.meanRate();
    }

    @Override
    public double oneMinuteRate() {
        return meter.oneMinuteRate();
    }

    /**
     * Returns the longest recorded duration.
     *
     * @return the longest recorded duration
     */
    @Override
    public double max() {
        return convertFromNS(histogram.max());
    }

    /**
     * Returns the shortest recorded duration.
     *
     * @return the shortest recorded duration
     */
    @Override
    public double min() {
        return convertFromNS(histogram.min());
    }

    /**
     * Returns the arithmetic mean of all recorded durations.
     *
     * @return the arithmetic mean of all recorded durations
     */
    @Override
    public double mean() {
        return convertFromNS(histogram.mean());
    }

    /**
     * Returns the standard deviation of all recorded durations.
     *
     * @return the standard deviation of all recorded durations
     */
    @Override
    public double stdDev() {
        return convertFromNS(histogram.stdDev());
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.core.Quantized#quantile(double)
     */
    @Override
    public double quantile(double quantile) {
        return quantiles(quantile)[0];
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.core.Quantized#quantiles(double)
     */
    @Override
    public Double[] quantiles(Double... quantiles) {
        final Double[] scores = histogram.quantiles(quantiles);
        for (int i = 0; i < scores.length; i++) {
            scores[i] = convertFromNS(scores[i]);
        }

        return scores;
    }

    @Override
    public String eventType() {
        return meter.eventType();
    }

    /**
     * Returns a list of all recorded durations in the timer's sample.
     *
     * @return a list of all recorded durations in the timer's sample
     */
    public List<Double> values() {
        final List<Double> values = new ArrayList<Double>();
        for (Long value : histogram.values()) {
            values.add(convertFromNS(value));
        }
        return values;
    }

    /**
     * Writes the values of the timer's sample to the given file.
     *
     * @param output the file to which the values will be written
     * @throws java.io.IOException if there is an error writing the values
     */
    public void dump(File output) throws IOException {
        histogram.dump(output);
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
