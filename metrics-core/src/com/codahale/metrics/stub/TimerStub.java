package com.codahale.metrics.stub;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.TimerContext;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics via {@link MeterStub}.
 */
public class TimerStub extends Timer {

	TimerContextStub mTimerContextStub = new TimerContextStub();;
	
    /**
     * Adds a recorded duration.
     *
     * @param duration the length of the duration
     * @param unit     the scale unit of {@code duration}
     */
    public void update(long duration, TimeUnit unit) {
    }

    /**
     * Times and records the duration of event.
     *
     * @param event a {@link Callable} whose {@link Callable#call()} method implements a process
     *              whose duration should be timed
     * @param <T>   the type of the value returned by {@code event}
     * @return the value returned by {@code event}
     * @throws Exception if {@code event} throws an {@link Exception}
     */
    public <T> T time(Callable<T> event) throws Exception {
    	return event.call();
    }

    /**
     * Returns a new {@link Context}.
     *
     * @return a new {@link Context}
     * @see Context
     */
    public TimerContext time() {
        return mTimerContextStub;
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public double getFifteenMinuteRate() {
        return 0.0;
    }

    @Override
    public double getFiveMinuteRate() {
        return 0.0;
    }

    @Override
    public double getMeanRate() {
        return 0.0;
    }

    @Override
    public double getOneMinuteRate() {
        return 0.0;
    }

    @Override
    public double getOneHourRate() {
        return 0.0;
    }

    @Override
    public double getThreeHourRate() {
        return 0.0;
    }    
    
    @Override
    public Snapshot getSnapshot() {
        return null;
    }
}
