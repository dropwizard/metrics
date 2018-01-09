package io.dropwizard.metrics5.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a method of an annotated object as metered.
 * <p/>
 * Given a method like this:
 * <pre><code>
 *     {@literal @}ExceptionMetered(name = "fancyName", cause=IllegalArgumentException.class)
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * A meter for the defining class with the name {@code fancyName} will be created and each time the
 * {@code #fancyName(String)} throws an exception of type {@code cause} (or a subclass), the meter
 * will be marked.
 * <p/>
 * A name for the metric can be specified as an annotation parameter, otherwise, the metric will be
 * named based on the method name.
 * <p/>
 * For instance, given a declaration of
 * <pre><code>
 *     {@literal @}ExceptionMetered
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * A meter named {@code fancyName.exceptions} will be created and marked every time an exception is
 * thrown.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface ExceptionMetered {
    /**
     * The default suffix for meter names.
     */
    String DEFAULT_NAME_SUFFIX = "exceptions";

    /**
     * @return The name of the meter. If not specified, the meter will be given a name based on the method
     * it decorates and the suffix "Exceptions".
     */
    String name() default "";

    /**
     * @return If {@code true}, use the given name as an absolute name. If {@code false}, use the given name
     * relative to the annotated class. When annotating a class, this must be {@code false}.
     */
    boolean absolute() default false;

    /**
     * @return The type of exceptions that the meter will catch and count.
     */
    Class<? extends Throwable> cause() default Exception.class;
}
