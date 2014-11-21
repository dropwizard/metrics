package com.codahale.metrics.graphite.deadqueue;

public class NoOperationDeadQueue implements DeadQueue {
    @Override
    public void add(Entry entry) {

    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Entry poll() {
        return null;
    }
}
