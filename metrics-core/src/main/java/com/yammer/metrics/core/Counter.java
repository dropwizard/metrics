package com.yammer.metrics.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An incrementing and decrementing counter metric.
 */
public class Counter implements Metric {
    private final AtomicLong count;

    Counter() {
        this.count = new AtomicLong(0);
    }

    /**
     * Increment the counter by one.
     */
    public void inc() {
        inc(1);
    }

    /**
     * Increment the counter by {@code n}.
     *
     * @param n the amount by which the counter will be increased
     */
    public void inc(long n) {
        count.addAndGet(n);
    }

    /**
     * Decrement the counter by one.
     */
    public void dec() {
        dec(1);
    }

    /**
     * Decrement the counter by {@code n}.
     *
     * @param n the amount by which the counter will be increased
     */
    public void dec(long n) {
        count.addAndGet(0 - n);
    }

    /**
     * Returns the counter's current value.
     *
     * @return the counter's current value
     */
    public long count() {
        return count.get();
    }

    /**
     * Resets the counter to 0.
     */
    public void clear() {
        count.set(0);
    }

    @Override
    public <T> void processWith(MetricProcessor<T> processor, MetricName name, T context) throws Exception {
        processor.processCounter(name, this, context);
    }
}
