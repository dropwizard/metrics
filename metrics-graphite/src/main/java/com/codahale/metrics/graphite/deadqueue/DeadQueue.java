package com.codahale.metrics.graphite.deadqueue;

public interface DeadQueue {

    void add(Entry entry);

    boolean isEmpty();

    Entry poll();
}
