package io.dropwizard.metrics5.jmx;

import io.dropwizard.metrics5.MetricName;
import java.util.Hashtable;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultObjectNameFactory implements ObjectNameFactory {

    private static final char[] QUOTABLE_CHARS = new char[] {',', '=', ':', '"'};
    private static final Logger LOGGER = LoggerFactory.getLogger(JmxReporter.class);

    @Override
    public ObjectName createName(String type, String domain, MetricName name) {
        try {
            ObjectName objectName;
            Hashtable<String, String> properties = new Hashtable<>();

            // Tags are copied first so name and type cannot be replaced by a tag of the same key.
            // Values that cannot be unquoted are quoted before construction; an illegal key is
            // skipped so it does not drop the rest of the name.
            for (Map.Entry<String, String> tag : name.getTags().entrySet()) {
                String key = tag.getKey();
                String value = tag.getValue();
                if (!isUsableTagKey(key) || value == null) {
                    continue;
                }
                properties.put(key, needsQuote(value) ? ObjectName.quote(value) : value);
            }
            properties.put("name", name.getKey());
            properties.put("type", type);
            objectName = new ObjectName(domain, properties);

            /*
             * The only way we can find out if we need to quote the properties is by
             * checking an ObjectName that we've constructed.
             */
            if (objectName.isDomainPattern()) {
                domain = ObjectName.quote(domain);
            }
            if (objectName.isPropertyValuePattern("name") || shouldQuote(objectName.getKeyProperty("name"))) {
                properties.put("name", ObjectName.quote(name.getKey()));
            }
            if (objectName.isPropertyValuePattern("type") || shouldQuote(objectName.getKeyProperty("type"))) {
                properties.put("type", ObjectName.quote(type));
            }
            objectName = new ObjectName(domain, properties);

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

    /**
     * Determines whether the value requires quoting.
     * According to the {@link ObjectName} documentation, values can be quoted or unquoted. Unquoted
     * values may not contain any of the characters comma, equals, colon, or quote.
     *
     * @param value a value to test
     * @return true when it requires quoting, false otherwise
     */
    private boolean shouldQuote(final String value) {
        for (char quotableChar : QUOTABLE_CHARS) {
            if (value.indexOf(quotableChar) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tag keys are ObjectName property keys, which cannot be quoted. Reserved keys are applied
     * separately so a tag cannot replace the metric name or type.
     */
    private boolean isUsableTagKey(final String key) {
        if (key == null || key.isEmpty() || "name".equals(key) || "type".equals(key)) {
            return false;
        }
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '*' || c == '?' || c == '\n' || c == '\r') {
                return false;
            }
            for (char quotableChar : QUOTABLE_CHARS) {
                if (c == quotableChar) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean needsQuote(final String value) {
        return shouldQuote(value) || value.indexOf('*') >= 0 || value.indexOf('?') >= 0;
    }

}
