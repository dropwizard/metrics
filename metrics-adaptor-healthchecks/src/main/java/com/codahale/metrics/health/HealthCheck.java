package com.codahale.metrics.health;

@Deprecated
public abstract class HealthCheck extends io.dropwizard.metrics.health.HealthCheck {

	@Deprecated
	public static class Result extends io.dropwizard.metrics.health.HealthCheck.Result {
		private static final Result HEALTHY = new Result(true, null, null);

		public static Result healthy() {
			return HEALTHY;
		}

		public static Result healthy(String message) {
			return new Result(true, message, null);
		}

		public static Result healthy(String message, Object... args) {
			return healthy(String.format(message, args));
		}

		public static Result unhealthy(String message) {
			return new Result(false, message, null);
		}

		public static Result unhealthy(String message, Object... args) {
			return unhealthy(String.format(message, args));
		}

		public static Result unhealthy(Throwable error) {
			return new Result(false, error.getMessage(), error);
		}

		private Result(boolean isHealthy, String message, Throwable error) {
			super(isHealthy, message, error);
		}
	}

	protected abstract Result check() throws Exception;

	public Result execute() {
		try {
			return check();
		} catch (Exception e) {
			return Result.unhealthy(e);
		}
	}
}
