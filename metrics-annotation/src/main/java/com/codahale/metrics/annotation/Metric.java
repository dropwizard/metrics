package com.codahale.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation requesting that a metric be injected or registered.
 *
 * <p/>
 * Given a field like this:
 * <pre><code>
 *     {@literal @}Metric
 *     public Histogram histogram;
 * </code></pre>
 * <p/>
 * A meter of the field's type will be created and injected into managed objects.
 * It will be up to the user to interact with the metric. This annotation
 * can be used on fields of type Meter, Timer, Counter, and Histogram.
 *
 * <p>
 * This may also be used to register a metric, which is useful for creating a histogram with
 * a custom Reservoir.
 * <pre><code>
 *     {@literal @}Metric
 *     public Histogram uniformHistogram = new Histogram(new UniformReservoir());
 * </code></pre>
 * </p>
 *
 * @since 3.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
public @interface Metric {

    /**
     * @return The name of the metric.
     */
    String name() default "";

    /**
     * @return If {@code true}, use the given name as an absolute name. If {@code false},
     * use the given name relative to the annotated class.
     */
    boolean absolute() default false;

}
