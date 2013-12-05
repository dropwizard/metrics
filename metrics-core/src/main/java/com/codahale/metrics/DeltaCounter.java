package com.codahale.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class DeltaCounter implements Metric, Counting {
    protected final AtomicLong count;

    public DeltaCounter() {
        this.count = new AtomicLong();
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
     * @param n the amount by which the counter will be decreased
     */
    public void dec(long n) {
        count.addAndGet(-n);
    }

    /**
     * Returns the counter's current value.
     *
     * @return the counter's current value
     */
    @Override
    public long getCount() {
        return count.get();
    }

    /**
     * get the counters current value and reset it to 0.
     *
     * @return the counter's value before the reset
     */
    public long getAndReset() {
        return count.getAndSet(0);
    }

}
