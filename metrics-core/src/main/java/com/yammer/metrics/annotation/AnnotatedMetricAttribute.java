/*
 * Copyright (c) 2012. Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.yammer.metrics.annotation;

import java.lang.reflect.AccessibleObject;

/**
 * Wrapper around an {@link java.lang.reflect.AccessibleObject} that is
 * annotated with {@link Publish}.
 */
public final class AnnotatedMetricAttribute {

    private final Object obj;
    private final Publish anno;
    private final AccessibleObject attr;
    private final String name;

    /**
     * Creates a new instance.
     */
    public AnnotatedMetricAttribute(Object obj, Publish anno, String name, AccessibleObject attr) {
        this.obj = obj;
        this.anno = anno;
        this.attr = attr;
        this.name = name;
        if (!attr.isAccessible()) attr.setAccessible(true);
    }

    /**
     * Returns the annotation on the attribute.
     */
    public Publish getAnnotation() {
        return anno;
    }

    /**
     * Returns the accessible object that is annotated.
     */
    public AccessibleObject getAttribute() {
        return attr;
    }

    /**
     * Returns the current value for the attribute.
     */
    public Object getValue() throws Exception {
        return AnnotationUtils.getValue(obj, attr);
    }

    /**
     * Returns the current value for the attribute as a number.
     */
    public Number getNumber() throws Exception {
        return AnnotationUtils.getNumber(obj, attr);
    }

    public String getName() {
        return name;
    }
}
