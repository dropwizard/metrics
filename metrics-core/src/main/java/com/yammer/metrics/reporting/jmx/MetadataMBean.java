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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.util.Map;

final class MetadataMBean implements DynamicMBean {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(MetadataMBean.class);

    private final ObjectName name;

    private final MBeanInfo beanInfo;

    private final Map<String,PublishedAttribute> attrs;

    MetadataMBean(
            ObjectName name,
            MBeanInfo beanInfo,
            Map<String, PublishedAttribute> attrs) {
        this.name = name;
        this.beanInfo = beanInfo;
        this.attrs = attrs;
    }

    public ObjectName getObjectName() {
        return name;
    }

    public Object getAttribute(String attribute)
            throws AttributeNotFoundException, MBeanException {
        PublishedAttribute attr = attrs.get(attribute);
        if (attr == null) {
            throw new AttributeNotFoundException(attribute);
        }
        try {
            return attr.getMetadata();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        for (String a : attributes) {
            try {
                list.add(new Attribute(a, getAttribute(a)));
            } catch (Exception e) {
                LOGGER.warn("getAttribute() failed for " + a, e);
            }
        }
        return list;
    }

    public MBeanInfo getMBeanInfo() {
        return beanInfo;
    }

    public Object invoke(
            String actionName, Object[] params, String[] signature) {
        throw new UnsupportedOperationException(
            "invoke(...) is not supported on this mbean");
    }

    public void setAttribute(Attribute attribute) {
        throw new UnsupportedOperationException(
            "setAttribute(...) is not supported on this mbean");
    }

    public AttributeList setAttributes(AttributeList attributes) {
        throw new UnsupportedOperationException(
            "setAttributes(...) is not supported on this mbean");
    }
}
