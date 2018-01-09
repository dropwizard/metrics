package io.dropwizard.metrics5.jmx;

import io.dropwizard.metrics5.MetricName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class DefaultObjectNameFactory implements ObjectNameFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmxReporter.class);

    @Override
    public ObjectName createName(String type, String domain, MetricName name) {
        try {
            ObjectName objectName = new ObjectName(domain, "name", name.getKey());
            if (objectName.isPattern()) {
                objectName = new ObjectName(domain, "name", ObjectName.quote(name.getKey()));
            }
            return objectName;
        } catch (MalformedObjectNameException e) {
            try {
                return new ObjectName(domain, "name", ObjectName.quote(name.getKey()));
            } catch (MalformedObjectNameException e1) {
                LOGGER.warn("Unable to register {} {}", type, name, e1);
                throw new RuntimeException(e1);
            }
        }
    }

}
