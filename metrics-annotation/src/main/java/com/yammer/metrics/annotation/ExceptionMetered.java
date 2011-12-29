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
 *     \@ExceptionMetered(name = "fancyName", eventType = "namings", rateUnit = TimeUnit.SECONDS,
 * cause=IllegalArgumentException.class)
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * A meter for the defining class with the name {@code fancyName} will be created and each time the
 * {@code #fancyName(String)} throws an exception of type {@code cause} (or a subclass), the meter
 * will be marked.
 * <p/>
 * By default, the annotation default to capturing all exceptions (subclasses of {@link Exception})
 * and will use the default event-type of "exceptions".
 * <p/>
 * A name for the metric can be specified as an annotation parameter, otherwise, the metric will be
 * named based on the method name.
 * <p/>
 * For instance, given a declaration of
 * <pre><code>
 *     \@ExceptionMetered
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * A meter named {@code fancyNameExceptionMetric} will be created with event-type named
 * "exceptions". The meter will be marked every time an exception is thrown.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionMetered {

    String DEFAULT_NAME_SUFFIX = "Exceptions";

    /**
     * The name of the meter. If not specified, the meter will be given a name based on the method
     * it decorates and the suffix "Exceptions".
     */
    String name() default "";

    /**
     * The name of the type of events the meter is measuring. The event type defaults to
     * "exceptions".
     */
    String eventType() default "exceptions";

    /**
     * The time unit of the meter's rate. Defaults to Seconds.
     */
    TimeUnit rateUnit() default TimeUnit.SECONDS;

    /**
     * The type of exceptions that the meter will catch and count.
     */
    Class<? extends Throwable> cause() default Exception.class;
}
