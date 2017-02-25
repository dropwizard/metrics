package com.codahale.metrics;

import java.util.concurrent.TimeUnit;

public interface MeasurementListener {
    /**
     * @param name the name of the metric
     * @param n the amount by which the counter was increased
     */
    void counterIncremented(String name, long n);

    /**
     * @param name the name of the metric
     * @param n the amount by which the counter was decreased
     */
    void counterDecremented(String name, long n);

    /**
     * @param name the name of the metric
     * @param duration the length of the duration
     * @param unit     the scale unit of {@code duration}
     */
    void timerUpdated(String name, long duration, TimeUnit unit);

    /**
     * @param name the name of the metric
     * @param value the length of the value
     */
    void histogramUpdated(String name, long value);

    /**
     * @param name the name of the metric
     * @param n the number of events
     */
    void meterMarked(String name, long n);
}
