package com.yammer.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a method of a Guice-provided object as counted.
 * <p/>
 * Given a method like this:
 * <pre><code>
 *     \@Counted(name = "fancyName")
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * A counter for the defining class with the name {@code fancyName} will be created and each time the
 * {@code #fancyName(String)} method is invoked, the current number of method executions will be counted.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Counted {

    /**
     * The group of the counter.
     */
    String group() default "";

    /**
     * The type of the counter.
     */
    String type() default "";

    /**
     * The name of the counter.
     */
    String name() default "";
    
}
