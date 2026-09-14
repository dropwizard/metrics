package io.dropwizard.metrics5.jmx;

import io.dropwizard.metrics5.MetricName;

import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default ObjectName factory.
 * Values are quoted when necessary. Illegal characters in domain and tag names are always replaced with '_'.
 */
public class DefaultObjectNameFactory implements ObjectNameFactory {

    // '"' is unsafe only when used at the beginning of a value, but is kept for consistency with old escaping
    private static final char[] QUOTABLE_CHARS = new char[]{',', '=', ':', '"', '*', '?', '\n'};
    // keys don't support quoting
    private static final Pattern INVALID_KEY_CHARS_PATTERN = Pattern.compile("[,=:*?\n]");
    // domain doesn't support quoting; ':' and '\n' can't be used, while '*' or '?' would turn it into a pattern
    private static final Pattern INVALID_DOMAIN_CHARS_PATTERN = Pattern.compile("[:*?\n]");

    private static final Logger LOGGER = LoggerFactory.getLogger(JmxReporter.class);

    @Override
    public ObjectName createName(String type, String domain, MetricName name) {
        try {
            Hashtable<String, String> properties = new Hashtable<>();

            name.getTags().forEach((key, value) -> {
                String sanitizedKey = key.isEmpty()
                        ? "_"
                        : sanitizeValue(key, INVALID_KEY_CHARS_PATTERN);
                properties.put(sanitizedKey, quoteIfNeeded(value));
            });

            properties.put("name", quoteIfNeeded(name.getKey()));
            properties.put("type", quoteIfNeeded(type));

            String sanitizedDomain = sanitizeValue(domain, INVALID_DOMAIN_CHARS_PATTERN);
            return new ObjectName(sanitizedDomain, properties);
        } catch (MalformedObjectNameException e) {
            LOGGER.warn("Unable to register {} {}", type, name, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Determines whether the value requires quoting.
     * According to the {@link ObjectName} documentation, values can be quoted or unquoted. Unquoted
     * values may not contain any of the characters: comma, equals, colon, quote or newline, and
     * turn the object name into a pattern if they contain an asterisk or a question mark.
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

    private String quoteIfNeeded(final String value) {
        return shouldQuote(value) ? ObjectName.quote(value) : value;
    }

    private String sanitizeValue(final String value, final Pattern pattern) {
        return pattern.matcher(value).replaceAll("_");
    }

}
