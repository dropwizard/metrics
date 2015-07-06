package com.codahale.metrics;

/**
 * An incrementing and decrementing counter metric.
 */
public class Counter implements Metric, Counting, Toggleable {
    private final LongAdder count;
    private boolean enabled = true;

    public Counter() {
        this.count = new LongAdder();
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
        if (enabled) {
            count.add(n);
        }
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
        if (enabled) {
            count.add(-n);
        }
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

    /**
     * Enable and disable this metric
     * @param enabled new value for enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            count.reset();
        }
        this.enabled = enabled;
    }
}
