package com.codahale.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation for marking a method as a gauge, which caches the result for a specified time.
 *
 * <p/>
 * Given a method like this:
 * <pre><code>
 *     {@literal @}CachedGauge(name = "queueSize", timeout = 30, timeoutUnit = TimeUnit.SECONDS)
 *     public int getQueueSize() {
 *         return queue.getSize();
 *     }
 *
 * </code></pre>
 * <p/>
 *
 * A gauge for the defining class with the name queueSize will be created which uses the annotated method's
 * return value as its value, and which caches the result for 30 seconds.
 *
 * @since 4.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CachedGauge {

    /**
     * The name of the counter.
     */
    String name() default "";

    /**
     * If {@code true}, use the given name as an absolute name. If {@code false}, use the given name
     * relative to the annotated class.
     */
    boolean absolute() default false;

    /**
     * The amount of time to cache the result
     */
    long timeout();

    /**
     * The unit of timeout
     */
    TimeUnit timeoutUnit() default TimeUnit.MILLISECONDS;

}
