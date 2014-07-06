package com.codahale.metrics.concrete;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Timer;
import com.codahale.metrics.TimerContext;

/**
 * A timing context.
 * 
 * @see Timer#time()
 */
public class TimerContextConcrete extends TimerContext {

	private final Timer timer;
	private final Clock clock;
	private final long startTime;

	TimerContextConcrete(Timer timer, Clock clock) {
		this.timer = timer;
		this.clock = clock;
		this.startTime = clock.getTick();
	}

	/**
	 * Stops recording the elapsed time, updates the timer and returns the
	 * elapsed time in nanoseconds.
	 */
	public long stop() {
		final long elapsed = clock.getTick() - startTime;
		timer.update(elapsed, TimeUnit.NANOSECONDS);
		return elapsed;
	}

	@Override
	public void close() {
		stop();
	}
}
