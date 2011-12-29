package com.yammer.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation for marking a method of a Guice-provided object as metered.
 * <p/>
 * Given a method like this:
 * <pre><code>
 *     \@Metered(name = "fancyName", eventType = "namings", rateUnit = TimeUnit.SECONDS)
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * A meter for the defining class with the name {@code fancyName} will be created and each time the
 * {@code #fancyName(String)} method is invoked, the meter will be marked.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Metered {
    /**
     * The name of the meter.
     */
    String name() default "";

    /**
     * The name of the type of events the meter is measuring.
     */
    String eventType() default "calls";

    /**
     * The time unit of the meter's rate.
     */
    TimeUnit rateUnit() default TimeUnit.SECONDS;
}
