package com.codahale.metrics.jmx;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultObjectNameFactory implements ObjectNameFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmxReporter.class);

    @Override
    public ObjectName createName(String type, String domain, String name) {
        try {
            ObjectName objectName;
            Hashtable<String, String> properties = new Hashtable<>();

            properties.put("name", name);
            properties.put("type", type);
            objectName = new ObjectName(domain, properties);

            /*
             * The only way we can find out if we need to quote the properties is by
             * checking an ObjectName that we've constructed.
             */
            if (objectName.isDomainPattern()) {
                domain = ObjectName.quote(domain);
            }
            if (objectName.isPropertyValuePattern("name")) {
                properties.put("name", ObjectName.quote(name));
            }
            if (objectName.isPropertyValuePattern("type")) {
                properties.put("type", ObjectName.quote(type));
            }
            objectName = new ObjectName(domain, properties);

            return objectName;
        } catch (MalformedObjectNameException e) {
            try {
                return new ObjectName(domain, "name", ObjectName.quote(name));
            } catch (MalformedObjectNameException e1) {
                LOGGER.warn("Unable to register {} {}", type, name, e1);
                throw new RuntimeException(e1);
            }
        }
    }

}
