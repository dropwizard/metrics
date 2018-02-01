package com.codahale.metrics;

@Deprecated
public interface Reservoir {

    int size();

    void update(long value);

    Snapshot getSnapshot();

    io.dropwizard.metrics5.Reservoir getDelegate();
}
