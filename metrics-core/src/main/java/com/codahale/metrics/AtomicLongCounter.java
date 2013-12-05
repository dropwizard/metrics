package com.codahale.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicLongCounter extends Counter {
    protected final AtomicLong alcount;

    public AtomicLongCounter() {
        this.alcount = new AtomicLong();
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
        alcount.addAndGet(n);
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
        alcount.addAndGet(-n);
    }

    /**
     * Returns the counter's current value.
     *
     * @return the counter's current value
     */
    @Override
    public long getCount() {
        return alcount.get();
    }

    public long getAndReset() {
        return alcount.getAndSet(0);
    }

}
