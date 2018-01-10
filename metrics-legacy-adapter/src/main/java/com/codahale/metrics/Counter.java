package com.codahale.metrics;

import static java.util.Objects.requireNonNull;

@Deprecated
public class Counter implements Metric, Counting {

    private final io.dropwizard.metrics5.Counter counter;

    public Counter() {
        this(new io.dropwizard.metrics5.Counter());
    }

    public Counter(io.dropwizard.metrics5.Counter counter) {
        this.counter = requireNonNull(counter);
    }

    public void inc() {
        counter.inc();
    }

    public void inc(long n) {
        counter.inc(n);
    }

    public void dec() {
        counter.dec();
    }

    public void dec(long n) {
        counter.dec(n);
    }

    @Override
    public long getCount() {
        return counter.getCount();
    }

    @Override
    public io.dropwizard.metrics5.Counter getDelegate() {
        return counter;
    }

}
