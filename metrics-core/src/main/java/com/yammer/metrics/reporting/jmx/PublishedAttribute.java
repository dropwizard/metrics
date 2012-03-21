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
package com.yammer.metrics.reporting.jmx;

import com.yammer.metrics.annotation.AnnotatedMetricAttribute;
import com.yammer.metrics.annotation.Publish;
import com.yammer.metrics.core.MetricName;

import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.*;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class PublishedAttribute {

    private static final String TYPE_NAME = "MetricMetadata";
    private static final String TYPE_DESC = "Metadata for a metric";
    private static final String[] ITEM_NAMES = {"Name", "Type", "Description", "Tags"};
    private static final OpenType<?>[] ITEM_TYPES;
    private static final CompositeType METADATA_TYPE;
    private static final Map<Class<?>, SimpleType<?>> TYPES = new HashMap<Class<?>, SimpleType<?>>(20);

    static {
        TYPES.put(BigDecimal.class, SimpleType.BIGDECIMAL);
        TYPES.put(BigInteger.class, SimpleType.BIGINTEGER);
        TYPES.put(Boolean.class, SimpleType.BOOLEAN);
        TYPES.put(Boolean.TYPE, SimpleType.BOOLEAN);
        TYPES.put(Byte.class, SimpleType.BYTE);
        TYPES.put(Byte.TYPE, SimpleType.BYTE);
        TYPES.put(Character.class, SimpleType.CHARACTER);
        TYPES.put(Character.TYPE, SimpleType.CHARACTER);
        TYPES.put(Date.class, SimpleType.DATE);
        TYPES.put(Double.class, SimpleType.DOUBLE);
        TYPES.put(Double.TYPE, SimpleType.DOUBLE);
        TYPES.put(Float.class, SimpleType.FLOAT);
        TYPES.put(Float.TYPE, SimpleType.FLOAT);
        TYPES.put(Integer.class, SimpleType.INTEGER);
        TYPES.put(Integer.TYPE, SimpleType.INTEGER);
        TYPES.put(Long.class, SimpleType.LONG);
        TYPES.put(Long.TYPE, SimpleType.LONG);
        TYPES.put(Short.class, SimpleType.SHORT);
        TYPES.put(Short.TYPE, SimpleType.SHORT);
        TYPES.put(String.class, SimpleType.STRING);

        try {
            ITEM_TYPES = new OpenType<?>[]{
                    SimpleType.STRING,                   // Name
                    SimpleType.STRING,                   // Type
                    SimpleType.STRING,                   // Description
                    new ArrayType(1, SimpleType.STRING)  // Tags
            };

            METADATA_TYPE = new CompositeType(
                    TYPE_NAME,
                    TYPE_DESC,
                    ITEM_NAMES,
                    ITEM_NAMES,
                    ITEM_TYPES);
        } catch (OpenDataException e) {
            // Should never happen unless there is a bug in the code
            throw new RuntimeException(e);
        }
    }


    private final AnnotatedMetricAttribute annotatedMetricAttribute;
    private final MBeanAttributeInfo metadataAttributeInfo;
    private final MBeanAttributeInfo valueAttributeInfo;
    private final CompositeDataSupport metadata;

    public PublishedAttribute(MetricName metricName, AnnotatedMetricAttribute annotatedMetricAttribute) {
        if (annotatedMetricAttribute == null) throw new IllegalArgumentException("attribute cannot be null");

        this.annotatedMetricAttribute = annotatedMetricAttribute;

        String name = metricName.getName();
        String type = metricName.getType();
        String desc = metricName.getGroup();

        metadataAttributeInfo = new OpenMBeanAttributeInfoSupport(
                name,
                "".equals(desc.trim()) ? name : desc,
                METADATA_TYPE,
                true,      // isReadable
                false,     // isWritable
                false);    // isIs

        valueAttributeInfo = new OpenMBeanAttributeInfoSupport(
                name,
                "".equals(desc.trim()) ? name : desc,
                getType(annotatedMetricAttribute.getAttribute()),
                true,      // isReadable
                false,     // isWritable
                false);    // isIs

        try {
            metadata = new CompositeDataSupport(
                    METADATA_TYPE,
                    ITEM_NAMES,
                    new Object[]{name, type, desc});
        } catch (OpenDataException e) {
            throw new IllegalArgumentException(
                    "failed to create mbean metadata value for " + toString(), e);
        }
    }

    public Publish getAnnotation() {
        return annotatedMetricAttribute.getAnnotation();
    }

    public Object getValue() throws Exception {
        return annotatedMetricAttribute.getValue();
    }

    public Number getNumber() throws Exception {
        return annotatedMetricAttribute.getNumber();
    }

    public CompositeDataSupport getMetadata() {
        return metadata;
    }

    public MBeanAttributeInfo getMetadataAttributeInfo() {
        return metadataAttributeInfo;
    }

    public MBeanAttributeInfo getValueAttributeInfo() {
        return valueAttributeInfo;
    }

    private OpenType<?> getType(AccessibleObject obj) {
        SimpleType<?> t = null;
        if (obj instanceof Field) {
            Field f = (Field) obj;
            t = TYPES.get(f.getType());
        } else {
            Method m = (Method) obj;
            t = TYPES.get(m.getReturnType());
        }
        return (t == null) ? SimpleType.STRING : t;
    }
}
