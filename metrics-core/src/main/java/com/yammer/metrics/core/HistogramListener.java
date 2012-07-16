package com.yammer.metrics.core;

/**
 * Listeners to {@link Histogram} state change events.
 */
public interface HistogramListener extends MetricListener {

    /**
     * Called after the {@link Histogram#clear()} event.
     * 
     * @param histogram
     *        the {@link Histogram} whose state was cleared
     */
    void onClear(Histogram histogram);

    /**
     * Called after update events, {@link Histogram#update(int)} or
     * {@link Histogram#update(long)}.
     * 
     * @param histogram
     *        the {@link Histogram} whose state has changed
     * @param value
     *        the value that has been added to the {@link Histogram}
     */
    void onUpdate(Histogram histogram, long value);
}
