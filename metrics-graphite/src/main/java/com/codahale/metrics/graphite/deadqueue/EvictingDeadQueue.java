package com.codahale.metrics.graphite.deadqueue;

import java.util.LinkedList;
import java.util.Queue;

public class EvictingDeadQueue implements DeadQueue {

    private final int maxSize;
    private final Queue<Entry> queue;

    public EvictingDeadQueue(int maxSize) {
        this.maxSize = maxSize;
        queue = new LinkedList<Entry>();
    }

    @Override
    public void add(Entry entry) {
        if (queue.size() >= maxSize) {
            queue.poll();
        }

        queue.add(entry);
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public Entry poll() {
        return queue.poll();
    }
}
