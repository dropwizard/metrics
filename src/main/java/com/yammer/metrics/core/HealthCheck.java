package com.yammer.metrics.core;

/**
 * A template class for an encapsulated service health check.
 *
 * @author coda
 */
public abstract class HealthCheck {
	public static class Result {
		private static final Result HEALTHY = new Result(true, null, null);
		private final boolean healthy;
		private final String message;
		private final Throwable error;

		public static Result healthy() {
			return HEALTHY;
		}

		public static Result unhealthy(String errorMessage) {
			return new Result(false, errorMessage, null);
		}

		public static Result unhealthy(Throwable error) {
			return new Result(false, error.getMessage(), error);
		}

		private Result(boolean isHealthy, String message, Throwable error) {
			this.healthy = isHealthy;
			this.message = message;
			this.error = error;
		}

		public boolean isHealthy() {
			return healthy;
		}

		public String getMessage() {
			return message;
		}

		public Throwable getError() {
			return error;
		}
	}

	public abstract String name();

	public abstract Result check() throws Exception;

	public Result execute() {
		try {
			return check();
		} catch (Throwable e) {
			return Result.unhealthy(e);
		}
	}
}
