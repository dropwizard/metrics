package com.codahale.metrics;

/**
 * An incrementing and decrementing counter metric.
 */
public class Counter implements Metric, Counting {
    private final LongAdderAdapter count;
    private final String name;
    private final MeasurementPublisher measurementPublisher;

    public Counter(String name, MeasurementPublisher measurementPublisher) {
        this.name = name;
        this.measurementPublisher = measurementPublisher;
        this.count = LongAdderProxy.create();
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
        count.add(n);
        measurementPublisher.counterIncremented(name, n);
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
     * @param n the amount by which the counter will be decreased
     */
    public void dec(long n) {
        count.add(-n);
        measurementPublisher.counterDecremented(name, n);
    }

    /**
     * Returns the counter's current value.
     *
     * @return the counter's current value
     */
    @Override
    public long getCount() {
        return count.sum();
    }
}
