package com.yammer.metrics.util;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadPools
{
    private final Set<ExecutorService> threadPools = new CopyOnWriteArraySet<ExecutorService>();

    /**
     * Creates a new scheduled thread pool of a given size with the given name.
     *
     * @param poolSize the number of threads to create
     * @param name the name of the pool
     * @return a new {@link ScheduledExecutorService}
     */
    public ScheduledExecutorService newScheduledThreadPool(int poolSize, String name) {
        final ScheduledExecutorService service = Executors.newScheduledThreadPool(poolSize, new NamedThreadFactory(name));
        threadPools.add(service);
        return service;
    }

    /**
     * Shuts down all thread pools created by this class in an orderly fashion.
     */
    public void shutdownThreadPools() {
        for (ExecutorService executor : threadPools) {
            executor.shutdown();
        }
    }

}
