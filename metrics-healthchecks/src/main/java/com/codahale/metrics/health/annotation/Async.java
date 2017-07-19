package com.codahale.metrics.health.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation for marking asynchronous health check execution.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Async {
    /**
     * Enum representing the initial health states.
     */
    enum InitialState {
        HEALTHY, UNHEALTHY
    }

    /**
     * Enum representing the possible schedule types.
     */
    enum ScheduleType {
        FIXED_RATE, FIXED_DELAY
    }

    /**
     * Period between executions.
     *
     * @return period
     */
    long period();

    /**
     * Scheduling type of asynchronous executions.
     *
     * @return schedule type
     */
    ScheduleType scheduleType() default ScheduleType.FIXED_RATE;

    /**
     * Initial delay of first execution.
     *
     * @return initial delay
     */
    long initialDelay() default 0;

    /**
     * Time unit of initial delay and period.
     *
     * @return time unit
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * Initial health state until first asynchronous execution completes.
     *
     * @return initial health state
     */
    InitialState initialState() default InitialState.HEALTHY;

}
