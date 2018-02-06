package io.dropwizard.metrics5.influxdb;

import io.dropwizard.metrics5.MetricAttribute;
import io.dropwizard.metrics5.MetricName;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A builder to create a "measurement line" according to the Influx DB line protocol.
 * <p>
 * Intended usage:
 * <pre>
 * // first:
 * builder.writeMeasurement(name);
 *
 * // then write one or more fields:
 * builder.writeFieldIfEnabled(key, value);
 * // ... or ...
 * builder.writeField(key).writeFieldValue(value);
 *
 * // finally:
 * builder.writeTimestampMillis(time);
 * if (builder.hasValues()) {
 *     StringBuilder sb = builder.get(); // the result is here
 * }
 * <pre>
 */
class InfluxDbLineBuilder {

    private final StringBuilder str = new StringBuilder();
    private boolean firstField;

    private final Set<MetricAttribute> disabledMetricAttributes;
    private final MetricName prefix;
    private final Map<MetricName, String> encodedNameCache = new WeakHashMap<>();

    InfluxDbLineBuilder(Set<MetricAttribute> disabledMetricAttributes, MetricName prefix) {
        this.disabledMetricAttributes = disabledMetricAttributes;
        this.prefix = prefix != null ? prefix : MetricName.empty();
    }

    InfluxDbLineBuilder writeMeasurement(MetricName name) {
        str.setLength(0);
        str.append(encodedNameCache.computeIfAbsent(name, this::writeMeasurementNoCache));
        str.append(' ');
        firstField = true;
        return this;
    }

    private String writeMeasurementNoCache(MetricName name) {
        StringBuilder sb = new StringBuilder();

        MetricName prefixedName = prefix.append(name);
        appendName(prefixedName.getKey(), sb);
        // InfluxDB Performance and Setup Tips:
        // Sort tags by key before sending them to the database. 
        // The sort should match the results from the Go bytes.Compare function.
        // ... tags are already sorted in MetricName
        for (Map.Entry<String, String> tag : prefixedName.getTags().entrySet()) {
            sb.append(',');
            appendName(tag.getKey(), sb);
            sb.append('=');
            appendName(tag.getValue(), sb);
        }
        return sb.toString();
    }

    InfluxDbLineBuilder writeField(MetricAttribute key) {
        if (!firstField) {
            str.append(',');
        }
        str.append(key.getCode()).append('=');
        firstField = false;
        return this;
    }

    InfluxDbLineBuilder writeField(String key) {
        if (!firstField) {
            str.append(',');
        }
        appendName(key, str);
        str.append('=');
        firstField = false;
        return this;
    }

    InfluxDbLineBuilder writeFieldValue(double value) {
        str.append(value);
        return this;
    }

    InfluxDbLineBuilder writeFieldValue(long value) {
        str.append(value).append('i');
        return this;
    }

    InfluxDbLineBuilder writeFieldValue(String value) {
        str.append('"');
        appendString(value, str);
        str.append('"');
        return this;
    }

    InfluxDbLineBuilder writeFieldValue(boolean value) {
        str.append(value ? 't' : 'f');
        return this;
    }

    InfluxDbLineBuilder writeTimestampMillis(long utcMillis) {
        str.append(' ').append(utcMillis).append("000000\n");
        return this;
    }

    InfluxDbLineBuilder writeFieldIfEnabled(MetricAttribute key, double value) {
        if (!disabledMetricAttributes.contains(key)) {
            writeField(key);
            writeFieldValue(value);
        }
        return this;
    }

    InfluxDbLineBuilder writeFieldIfEnabled(MetricAttribute key, long value) {
        if (!disabledMetricAttributes.contains(key)) {
            writeField(key);
            writeFieldValue(value);
        }
        return this;
    }

    boolean hasValues() {
        return !firstField;
    }

    StringBuilder get() {
        return str;
    }

    private static void appendName(CharSequence field, StringBuilder dst) {
        int len = field.length();
        for (int i = 0; i < len; i++) {
            char ch = field.charAt(i);
            if (ch == ',' || ch == '=' || ch == ' ') {
                // escape
                dst.append('\\');
            }
            dst.append(ch);
        }
    }

    private static void appendString(CharSequence field, StringBuilder dst) {
        int len = field.length();
        for (int i = 0; i < len; i++) {
            char ch = field.charAt(i);
            if (ch == '"') {
                // escape
                dst.append('\\');
            }
            dst.append(ch);
        }
    }

}
