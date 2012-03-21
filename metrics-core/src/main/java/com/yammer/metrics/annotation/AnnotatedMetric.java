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

import java.util.List;

/**
 * Wrapper around an object that is annotated to make it easy to access the
 * annotated fields.
 */
public final class AnnotatedMetric {

    private final Object object;
    private final List<AnnotatedMetricAttribute> attrs;

    public AnnotatedMetric(Object obj) {
        if(obj == null){
            throw new IllegalArgumentException("obj cannot be null");
        }

        this.object = obj;

        attrs = AnnotationUtils.getAttributesToPublish(obj);
    }

    /** Returns the wrapped object. */
    public Object getObject() {
        return object;
    }

    /** Returns the attributes with {@link Publish} annotations. */
    public List<AnnotatedMetricAttribute> getAttributes() {
        return attrs;
    }

    /** Returns the canonical class name of the wrapped class. */
    public String getClassName() {
        return object.getClass().getCanonicalName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotatedMetric that = (AnnotatedMetric) o;

        if (!object.equals(that.object)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }
}
