package com.codahale.metrics;

import java.util.concurrent.ThreadFactory;

import static java.util.Objects.requireNonNull;

@Deprecated
public class InstrumentedThreadFactory implements ThreadFactory {

    private final io.dropwizard.metrics5.InstrumentedThreadFactory delegate;

    public InstrumentedThreadFactory(io.dropwizard.metrics5.InstrumentedThreadFactory delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public InstrumentedThreadFactory(ThreadFactory delegate, MetricRegistry registry) {
        this(new io.dropwizard.metrics5.InstrumentedThreadFactory(delegate, registry.getDelegate()));
    }

    public InstrumentedThreadFactory(ThreadFactory delegate, MetricRegistry registry, String name) {
        this(new io.dropwizard.metrics5.InstrumentedThreadFactory(delegate, registry.getDelegate(), name));
    }

    @Override
    public Thread newThread(Runnable r) {
        return delegate.newThread(r);
    }
}
