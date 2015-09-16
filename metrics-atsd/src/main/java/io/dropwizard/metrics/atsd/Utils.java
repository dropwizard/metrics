package io.dropwizard.metrics.atsd;

import io.dropwizard.metrics.MetricName;

import java.util.regex.Pattern;

public class Utils {
    private static final Pattern SPACE = Pattern.compile("[[\\s]]");
    private static final Pattern QUOTES = Pattern.compile("[[\'|\"]]");
    private static final Pattern D_QUOTE = Pattern.compile("[[\"]]");

    public static String sanitizeEntity(String s) {
        return sanitize(s);
    }

    public static String sanitizeMetric(String s) {
        return sanitize(s);
    }

    public static String sanitizeTagKey(String s) {
        return sanitize(s);
    }

    public static String sanitizeTagValue(String s) {
        s = D_QUOTE.matcher(s).replaceAll("\\\\\"");
        if (s.contains(" "))
            s = new StringBuilder("\"").append(s).append("\"").toString();
        return s;
    }

    protected static String sanitize(String s) {
        s = SPACE.matcher(s).replaceAll("_");
        s = QUOTES.matcher(s).replaceAll("");
        return s;
    }

    public static String composeMessage(String entity, MetricName metric,
                                        String value, long timestamp) {
        StringBuilder builder = new StringBuilder();
        String metricName = sanitizeMetric(metric.getKey());
        builder.append("series e:").append(sanitizeEntity(entity))
                .append(" m:").append(metricName).append("=").append(value).append(" ");
        for (java.util.Map.Entry<String, String> entry : metric.getTags().entrySet()) {
            String tagName = sanitizeTagKey(entry.getKey());
            String tagValue = sanitizeTagValue(entry.getValue());
            builder.append("t:").append(tagName).append("=").append(tagValue).append(" ");
        }
        builder.append("ms:").append(String.valueOf(timestamp)).append('\n');
        return builder.toString();
    }
}
