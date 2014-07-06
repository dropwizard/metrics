package com.codahale.metrics;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics via {@link Meter}.
 */
public abstract class Timer implements Metered, Sampling {

    /**
     * Adds a recorded duration.
     *
     * @param duration the length of the duration
     * @param unit     the scale unit of {@code duration}
     */
    public abstract void update(long duration, TimeUnit unit);

    /**
     * Times and records the duration of event.
     *
     * @param event a {@link Callable} whose {@link Callable#call()} method implements a process
     *              whose duration should be timed
     * @param <T>   the type of the value returned by {@code event}
     * @return the value returned by {@code event}
     * @throws Exception if {@code event} throws an {@link Exception}
     */
    public abstract <T> T time(Callable<T> event) throws Exception;

    /**
     * Returns a new {@link Context}.
     *
     * @return a new {@link Context}
     * @see Context
     */
    public abstract TimerContext time();

    @Override
    public abstract long getCount();

    @Override
    public abstract double getFifteenMinuteRate();

    @Override
    public abstract double getFiveMinuteRate();

    @Override
    public abstract double getMeanRate();

    @Override
    public abstract double getOneMinuteRate();

    @Override
    public abstract double getOneHourRate();

    @Override
    public abstract double getThreeHourRate();
    
    @Override
    public abstract Snapshot getSnapshot();
}
