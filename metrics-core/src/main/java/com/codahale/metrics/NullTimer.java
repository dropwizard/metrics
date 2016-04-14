package com.codahale.metrics;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class NullTimer extends Timer {
	private final Snapshot snapshot;
	private final double rate;

	/**
	 * Initializes the timer's histogram to contain a single 0 and rate to 0.0.
	 */
	public NullTimer() {
		this(new long[] { 0 }, 0.0);
	}

	/**
	 * Initializes the timer's rate with the given value and the histogram to contain a single 0.
	 * 
	 * @param rateValue
	 */
	public NullTimer(double rateValue) {
		this(new long[] { 0 }, rateValue);
	}

	/**
	 * Initializes the timer's histogram with the given inputs and the rate to 0.0.
	 * 
	 * @param histogramValues
	 */
	public NullTimer(long[] histogramValues) {
		this(histogramValues, 0.0);
	}

	/**
	 * Initializes the timer's values to the given inputs.
	 * 
	 * @param histogramValues the timer's histogram's constant values
	 * @param rateValue       the timer's constant rate
	 */
	public NullTimer(long[] histogramValues, double rateValue) {
		this.snapshot = new UniformSnapshot(histogramValues);
		rate = rateValue;
	}

    /**
     * Does nothing.
     * 
     * @param duration not used
     * @param unit     not used
     */
    @Override
	public void update(long duration, TimeUnit unit) {
    }

    /**
     * Calls event.call() and returns the result.
     * 
     * @param event a {@link Callable} whose {@link Callable#call()} should be called
     * @param <T>   the type of the value returned by {@code event}
     * @return the value returned by {@code event}
     * @throws Exception if {@code event} throws an {@link Exception}
     */
    @Override
	public <T> T time(Callable<T> event) throws Exception {
        return event.call();
    }

    /**
     * Returns the timer's constant number of values recorded.
     * 
     * @return the timer's constant number of values recorded
     */
    @Override
    public long getCount() {
        return snapshot.size();
    }

    /**
     * Returns the timer's rate constant.
     * 
     * @return the timer's rate constant
     */
    @Override
    public double getFifteenMinuteRate() {
        return rate;
    }

    /**
     * Returns the timer's rate constant.
     * 
     * @return the timer's rate constant
     */
    @Override
    public double getFiveMinuteRate() {
        return rate;
    }

    /**
     * Returns the timer's rate constant.
     * 
     * @return the timer's rate constant
     */
    @Override
    public double getMeanRate() {
        return rate;
    }

    /**
     * Returns the timer's rate constant.
     * 
     * @return the timer's rate constant
     */
    @Override
    public double getOneMinuteRate() {
        return rate;
    }

    /**
     * Returns a snapshot representing the timer's histogram's constant values.
     *
     * @return a snapshot representing the timer's histogram's constant values
     */
    @Override
    public Snapshot getSnapshot() {
        return snapshot;
    }
}
