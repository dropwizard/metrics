package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;

/**
 * An object which maintains mean and exponentially-weighted rate.
 */
public interface Metered extends Metric {
    /**
     * Returns the meter's rate unit.
     *
     * @return the meter's rate unit
     */
    TimeUnit rateUnit();

    /**
     * Returns the type of events the meter is measuring.
     *
     * @return the meter's event type
     */
    String eventType();

    /**
     * Returns the number of events which have been marked.
     *
     * @return the number of events which have been marked
     */
    long count();

    /**
     * Returns the fifteen-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the fifteen-minute load average in the
     * {@code top} Unix command.
     *
     * @return the fifteen-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    double fifteenMinuteRate();

    /**
     * Returns the five-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the five-minute load average in the {@code
     * top} Unix command.
     *
     * @return the five-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    double fiveMinuteRate();

    /**
     * Returns the mean rate at which events have occurred since the meter was created.
     *
     * @return the mean rate at which events have occurred since the meter was created
     */
    double meanRate();

    /**
     * Returns the one-minute exponentially-weighted moving average rate at which events have
     * occurred since the meter was created.
     * <p/>
     * This rate has the same exponential decay factor as the one-minute load average in the {@code
     * top} Unix command.
     *
     * @return the one-minute exponentially-weighted moving average rate at which events have
     *         occurred since the meter was created
     */
    double oneMinuteRate();
}
