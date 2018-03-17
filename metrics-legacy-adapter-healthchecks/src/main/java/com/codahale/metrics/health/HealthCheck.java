package com.codahale.metrics.health;

import java.util.Map;
import java.util.Objects;

@Deprecated
public abstract class HealthCheck {

    public static class Result {

        public static HealthCheck.Result healthy() {
            return new Result(io.dropwizard.metrics5.health.HealthCheck.Result.healthy());
        }

        public static HealthCheck.Result healthy(String message) {
            return new Result(io.dropwizard.metrics5.health.HealthCheck.Result.healthy(message));
        }

        public static HealthCheck.Result healthy(String message, Object... args) {
            return new Result(io.dropwizard.metrics5.health.HealthCheck.Result.healthy(message, args));
        }

        public static HealthCheck.Result unhealthy(String message) {
            return new Result(io.dropwizard.metrics5.health.HealthCheck.Result.unhealthy(message));
        }

        public static HealthCheck.Result unhealthy(String message, Object... args) {
            return new Result(io.dropwizard.metrics5.health.HealthCheck.Result.unhealthy(message, args));
        }

        public static HealthCheck.Result unhealthy(Throwable error) {
            return new Result(io.dropwizard.metrics5.health.HealthCheck.Result.unhealthy(error));
        }

        public static HealthCheck.Result of(io.dropwizard.metrics5.health.HealthCheck.Result delegate) {
            return new Result(delegate);
        }

        public static HealthCheck.ResultBuilder builder() {
            return new HealthCheck.ResultBuilder();
        }

        private final io.dropwizard.metrics5.health.HealthCheck.Result delegate;

        private Result(io.dropwizard.metrics5.health.HealthCheck.Result delegate) {
            this.delegate = delegate;
        }

        private Result(io.dropwizard.metrics5.health.HealthCheck.ResultBuilder builder) {
            this.delegate = builder.build();
        }

        public boolean isHealthy() {
            return delegate.isHealthy();
        }

        public String getMessage() {
            return delegate.getMessage();
        }

        public Throwable getError() {
            return delegate.getError();
        }

        public String getTimestamp() {
            return delegate.getTimestamp();
        }

        public Map<String, Object> getDetails() {
            return delegate.getDetails();
        }

        public io.dropwizard.metrics5.health.HealthCheck.Result getDelegate() {
            return delegate;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Result) {
                final Result that = (Result) o;
                return Objects.equals(delegate, that.delegate);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(delegate);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    public static class ResultBuilder {

        private io.dropwizard.metrics5.health.HealthCheck.ResultBuilder delegate;

        protected ResultBuilder() {
            delegate = io.dropwizard.metrics5.health.HealthCheck.Result.builder();
        }

        public HealthCheck.ResultBuilder healthy() {
            delegate.healthy();
            return this;
        }

        public HealthCheck.ResultBuilder unhealthy() {
            delegate.unhealthy();
            return this;
        }

        public HealthCheck.ResultBuilder unhealthy(Throwable error) {
            delegate.unhealthy(error);
            return this;
        }

        public HealthCheck.ResultBuilder withMessage(String message) {
            delegate.withMessage(message);
            return this;
        }

        public HealthCheck.ResultBuilder withMessage(String message, Object... args) {
            delegate.withMessage(message, args);
            return this;
        }

        public HealthCheck.ResultBuilder withDetail(String key, Object data) {
            delegate.withDetail(key, data);
            return this;
        }

        public HealthCheck.Result build() {
            return new HealthCheck.Result(delegate);
        }
    }

    protected abstract HealthCheck.Result check() throws Exception;

    public HealthCheck.Result execute() {
        try {
            return check();
        } catch (Exception e) {
            return HealthCheck.Result.unhealthy(e);
        }
    }

    public io.dropwizard.metrics5.health.HealthCheck transform() {
        final HealthCheck original = this;
        return () -> original.check().delegate;
    }

    public static HealthCheck of(io.dropwizard.metrics5.health.HealthCheck delegate) {
        return new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return new Result(delegate.execute());
            }
        };
    }
}
