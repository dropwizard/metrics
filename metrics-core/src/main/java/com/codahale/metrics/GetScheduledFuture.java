package com.codahale.metrics;


import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface GetScheduledFuture<T> {
    ScheduledFuture<T> schedule(Long delay, Long period, TimeUnit unit, Runnable runnable, ScheduledExecutorService executor);
}
