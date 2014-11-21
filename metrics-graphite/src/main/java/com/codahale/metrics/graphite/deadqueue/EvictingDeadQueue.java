package com.codahale.metrics.graphite.deadqueue;


import com.google.common.collect.EvictingQueue;

public class EvictingDeadQueue implements DeadQueue {

    private final EvictingQueue<Entry> queue;

    public EvictingDeadQueue(int maxSize) {
        queue = EvictingQueue.create(maxSize);
    }

    @Override
    public void add(Entry entry) {
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
