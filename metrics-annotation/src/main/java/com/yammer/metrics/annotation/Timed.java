package com.yammer.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation for marking a method of a Guice-provided object as timed.
 * <p/>
 * Given a method like this:
 * <pre><code>
 *     \@Timed(name = "fancyName", rateUnit = TimeUnit.SECONDS, durationUnit =
 * TimeUnit.MICROSECONDS)
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * A timer for the defining class with the name {@code fancyName} will be created and each time the
 * {@code #fancyName(String)} method is invoked, the method's execution will be timed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timed {
    /**
     * The name of the timer.
     */
    String name() default "";

    /**
     * The time unit of the timer's rate.
     */
    TimeUnit rateUnit() default TimeUnit.SECONDS;

    /**
     * The time unit of the timer's duration.
     */
    TimeUnit durationUnit() default TimeUnit.MILLISECONDS;
}
