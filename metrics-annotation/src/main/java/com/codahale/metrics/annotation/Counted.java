package com.codahale.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a method of an annotated object as counted.
 *
 * <p/>
 * Given a method like this:
 * <pre><code>
 *     {@literal @}Counted(name = "fancyName")
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * A counter for the defining class with the name {@code fancyName} will be created and each time the
 * {@code #fancyName(String)} method is invoked, the counter will be marked.
 *
 * @since 4.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Counted {

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
     * If {@code false} (default), the counter is decremented when the annotated
     * method returns, counting current invocations of the annotated method.
     * If {@code true}, the counter increases monotonically, counting total
     * invocations of the annotated method.
     */
    boolean monotonic() default false;

}
