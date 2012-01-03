package com.yammer.metrics.core;

/**
 * A processor of metric instances.
 *
 * @param <T>
 */
public interface MetricProcessor<T> {
    /**
     * Process the given {@link Metered} instance.
     *
     * @param name       the name of the meter
     * @param meter      the meter
     * @param context    the context of the meter
     * @throws Exception if something goes wrong
     */
    void processMeter(MetricName name, Metered meter, T context) throws Exception;

    /**
     * Process the given counter.
     *
     * @param name       the name of the counter
     * @param counter    the counter
     * @param context    the context of the meter
     * @throws Exception if something goes wrong
     */
    void processCounter(MetricName name, Counter counter, T context) throws Exception;

    /**
     * Process the given histogram.
     *
     * @param name       the name of the histogram
     * @param histogram  the histogram
     * @param context    the context of the meter
     * @throws Exception if something goes wrong
     */
    void processHistogram(MetricName name, Histogram histogram, T context) throws Exception;

    /**
     * Process the given timer.
     *
     * @param name       the name of the timer
     * @param timer      the timer
     * @param context    the context of the meter
     * @throws Exception if something goes wrong
     */
    void processTimer(MetricName name, Timer timer, T context) throws Exception;

    /**
     * Process the given gauge.
     *
     * @param name       the name of the gauge
     * @param gauge      the gauge
     * @param context    the context of the meter
     * @throws Exception if something goes wrong
     */
    void processGauge(MetricName name, Gauge<?> gauge, T context) throws Exception;
}
