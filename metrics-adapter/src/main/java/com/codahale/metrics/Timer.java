package com.codahale.metrics;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.dropwizard.metrics.Metered;
import io.dropwizard.metrics.Sampling;

@Deprecated
public class Timer implements Metered, Metric, Sampling {
	final io.dropwizard.metrics.Timer timer;

	public static class Context implements AutoCloseable {
		final io.dropwizard.metrics.Timer.Context context;

		public Context(io.dropwizard.metrics.Timer.Context context) {
			this.context = context;
		}

		public long stop() {
			return context.stop();
		}

		@Override
		public void close() {
			context.close();
		}
	}

	public Timer(io.dropwizard.metrics.Timer timer) {
		this.timer = timer;
	}

	@Override
	public Snapshot getSnapshot() {
		return new Snapshot(timer.getSnapshot());
	}

	@Override
	public long getCount() {
		return timer.getCount();
	}

	@Override
	public double getFifteenMinuteRate() {
		return timer.getFifteenMinuteRate();
	}

	@Override
	public double getFiveMinuteRate() {
		return timer.getFiveMinuteRate();
	}

	@Override
	public double getMeanRate() {
		return timer.getMeanRate();
	}

	@Override
	public double getOneMinuteRate() {
		return timer.getOneMinuteRate();
	}

	public void update(long duration, TimeUnit unit) {
		timer.update(duration, unit);
	}

	public <T> T time(Callable<T> event) throws Exception {
		return timer.time(event);
	}

	public void time(Runnable event) {
		timer.time(event);
	}

	public Context time() {
		return new Context(timer.time());
	}
}
