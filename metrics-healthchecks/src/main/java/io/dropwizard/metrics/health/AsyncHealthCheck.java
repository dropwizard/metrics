package io.dropwizard.metrics.health;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AsyncHealthCheck extends HealthCheck implements Runnable {
    private final HealthCheck synchronousHealthCheck;
    private final int maxStaleAge;
    private final TimeUnit maxStaleAgeUnit;

    private volatile CachedResult lastResult;

    public AsyncHealthCheck(HealthCheck synchronousHealthCheck, int maxStaleAge, TimeUnit maxStaleAgeUnit) {
        this.synchronousHealthCheck = synchronousHealthCheck;
        this.maxStaleAge = maxStaleAge;
        this.maxStaleAgeUnit = maxStaleAgeUnit;
    }

    @Override
    protected Result check() throws Exception {
        if (new Date(lastResult.whenCheckFinished.getTime() + maxStaleAgeUnit.toSeconds(maxStaleAge)).before(new Date())) {
            return Result.unhealthy("Stale");
        } else {
            return lastResult.result;
        }
    }

    @Override
    public void run() {
        Result check = synchronousHealthCheck.execute();
        lastResult = new CachedResult(check, new Date());
    }

    private static class CachedResult {
        public final Result result;
        public final Date whenCheckFinished;

        private CachedResult(Result result, Date whenCheckFinished) {
            this.result = result;
            this.whenCheckFinished = whenCheckFinished;
        }
    }
}
