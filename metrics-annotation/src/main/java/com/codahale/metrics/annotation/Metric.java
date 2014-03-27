/**
 * Copyright (C) 2012 Ryan W Tenney (ryan@10e.us)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * @since 4.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Metric {

    /**
     * The name of the metric.
     */
    String name() default "";

    /**
     * If {@code true}, use the given name as an absolute name. If {@code false},
     * use the given name relative to the annotated class.
     */
    boolean absolute() default false;

}
