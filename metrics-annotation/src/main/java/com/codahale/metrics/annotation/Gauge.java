package com.codahale.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a method of an annotated object as a gauge.
 * <p/>
 * Given a method like this:
 * <pre><code>
 *     {@literal @}Gauge(name = "queueSize")
 *     public int getQueueSize() {
 *         return queue.size;
 *     }
 * </code></pre>
 * <p/>
 * A gauge for the defining class with the name {@code queueSize} will be created which uses the
 * annotated method's return value as its value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Gauge {
    /**
     * The gauge's name.
     */
    String name() default "";

    /**
     * If {@code true}, use the given name as an absolute name. If {@code false}, use the given name
     * relative to the annotated class.
     */
    boolean absolute() default false;
}
