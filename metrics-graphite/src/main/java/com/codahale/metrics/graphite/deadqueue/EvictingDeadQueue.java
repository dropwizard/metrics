package com.codahale.metrics.graphite.deadqueue;

import java.util.LinkedList;
import java.util.Queue;

public class EvictingDeadQueue implements DeadQueue {

    private final int maxSize;
    private final Queue<Entry> list;

    public EvictingDeadQueue(int maxSize) {
        this.maxSize = maxSize;
        list = new LinkedList<Entry>();
    }

    @Override
    public void add(Entry entry) {
        if (list.size() >= maxSize) {
            list.poll();
        }

        list.add(entry);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Entry poll() {
        return list.poll();
    }
}
