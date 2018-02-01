package com.codahale.metrics;

@Deprecated
public interface Sampling {

    Snapshot getSnapshot();
}
