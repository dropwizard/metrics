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

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper functions for querying the monitor annotations associated with a
 * class.
 */
public final class AnnotationUtils {
    private AnnotationUtils() {
    }

    /**
     * Return the list of fields/methods annotated with {@link Publish}.
     */
    public static List<AnnotatedMetricAttribute> getAttributesToPublish(Object obj) {
        Map<String,AccessibleObject> fields = getAnnotatedFields(Publish.class, obj, Integer.MAX_VALUE);
        ArrayList<AnnotatedMetricAttribute> attrs = new ArrayList<AnnotatedMetricAttribute>(fields.size());

        for (Map.Entry<String, AccessibleObject> entry : fields.entrySet()) {
            Publish p = entry.getValue().getAnnotation(Publish.class);
            attrs.add(new AnnotatedMetricAttribute(obj, p, entry.getKey(), entry.getValue()));
        }

        return attrs;
    }

    /**
     * Check that the object conforms to annotation requirements.
     */
    public static void validate(Object obj) {

        List<AnnotatedMetricAttribute> attrs = getAttributesToPublish(obj);
        if (attrs.isEmpty()) {
            throw new IllegalArgumentException(
                    "no Monitor annotations on object " + obj);
        }
        String ctype = obj.getClass().getCanonicalName();
        for (AnnotatedMetricAttribute attr : attrs) {
            Object value = null;
            try {
                value = attr.getValue();
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "failed to get value for " + attr.getAnnotation() + " on " + ctype, e);
            }
        }
    }

    /**
     * Try to convert an object into a number. Boolean values will return 1 if
     * true and 0 if false. If the value is null or an unknown data type null
     * will be returned.
     */
    public static Number asNumber(Object value) {
        Number num = null;
        if (value == null) {
            num = null;
        } else if (value instanceof Number) {
            num = (Number) value;
        } else if (value instanceof Boolean) {
            num = ((Boolean) value) ? 1 : 0;
        }
        return num;
    }

    /**
     * Get the value of a field or accessor method of {@code obj} identified
     * by {@code attr}.
     *
     * @param obj  the instance to query
     * @param attr the field or method to retrieve
     * @return value of the field or method
     */
    public static Object getValue(Object obj, AccessibleObject attr)
            throws Exception {
        return (attr instanceof Field)
                ? ((Field) attr).get(obj)
                : ((Method) attr).invoke(obj);
    }

    /**
     * Get the value of a field or accessor method of {@code obj} identified
     * by {@code attr} as a number. See {@link #asNumber} for details on the
     * conversion.
     *
     * @param obj  the instance to query
     * @param attr the field or method to retrieve
     * @return value of the field or method
     */
    public static Number getNumber(Object obj, AccessibleObject attr)
            throws Exception {
        return asNumber(getValue(obj, attr));
    }

    /**
     * Helper to return all fields or methods that have the specified
     * annotation.
     *
     * @param annotationClass the type of annotation to check for
     * @param obj             instance to query
     * @param maxPerClass     getMax number of annotated attributes that are
     *                        permitted for this class
     * @return map of name to matching attributes. Note if a field and a method have the same name, the method will win.
     */
    private static Map<String,AccessibleObject> getAnnotatedFields(Class<? extends Annotation> annotationClass,
            Object obj, int maxPerClass) {
        Map<String, AccessibleObject> attrs = new HashMap<String, AccessibleObject> ();

        // Fields
        Class<?> objClass = obj.getClass();
        for (Field field : objClass.getDeclaredFields()) {
            Object annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                attrs.put(field.getName(), field);
            }
        }

        // Methods
        for (Method method : objClass.getDeclaredMethods()) {
            Object annotation = method.getAnnotation(annotationClass);
            if (annotation != null) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                attrs.put(method.getName(), method);
            }
        }


        if (attrs.size() > maxPerClass) {
            throw new IllegalArgumentException(String.format(
                    "class %s has %d attributes annotated with %s",
                    obj.getClass().getCanonicalName(),
                    attrs.size(),
                    annotationClass.getCanonicalName()));
        }
        return attrs;
    }
}
