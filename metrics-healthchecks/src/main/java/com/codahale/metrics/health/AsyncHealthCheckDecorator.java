package com.codahale.metrics.health;

import com.codahale.metrics.health.annotation.Async;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * A health check decorator to manage asynchronous executions.
 */
public class AsyncHealthCheckDecorator extends HealthCheck implements Runnable {
    private static final String NO_RESULT_YET_MESSAGE = "Waiting for first asynchronous check result.";
    private final HealthCheck healthCheck;
    private final ScheduledFuture<?> future;
    private volatile Result result;

    AsyncHealthCheckDecorator(HealthCheck healthCheck, ScheduledExecutorService executorService) {
        check(healthCheck != null, "healthCheck cannot be null");
        check(executorService != null, "executorService cannot be null");
        Async async = healthCheck.getClass().getAnnotation(Async.class);
        check(async != null, "healthCheck must contain Async annotation");
        check(async.period() > 0, "period cannot be less than or equal to zero");
        check(async.initialDelay() >= 0, "initialDelay cannot be less than zero");

        this.healthCheck = healthCheck;
        result = Async.InitialState.HEALTHY.equals(async.initialState()) ? Result.healthy(NO_RESULT_YET_MESSAGE) :
                Result.unhealthy(NO_RESULT_YET_MESSAGE);
        if (Async.ScheduleType.FIXED_RATE.equals(async.scheduleType())) {
            future = executorService.scheduleAtFixedRate(this, async.initialDelay(), async.period(), async.unit());
        } else {
            future = executorService.scheduleWithFixedDelay(this, async.initialDelay(), async.period(), async.unit());
        }

    }

    @Override
    public void run() {
        result = healthCheck.execute();
    }

    @Override
    protected Result check() throws Exception {
        return result;
    }

    boolean tearDown() {
        return future.cancel(true);
    }

    public HealthCheck getHealthCheck() {
        return healthCheck;
    }

    private static void check(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

}
