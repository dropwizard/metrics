package io.dropwizard.metrics5;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A metric name with the ability to include semantic tags.
 * This replaces the previous style where metric names where strictly dot-separated strings.
 */
public class MetricName implements Comparable<MetricName> {

    private static final String SEPARATOR = ".";
    private static final SortedMap<String, String> EMPTY_TAGS = Collections.emptySortedMap();
    static final MetricName EMPTY = new MetricName("", EMPTY_TAGS);

    /**
     * Returns an empty metric name.
     *
     * @return an empty metric name.
     */
    public static MetricName empty() {
        return EMPTY;
    }

    private final String key;
    private final SortedMap<String, String> tags;

    public MetricName(String key, Map<String, String> tags) {
        this.key = Objects.requireNonNull(key, "Metric key must not be null");
        if (tags == null || tags.isEmpty()) {
            this.tags = EMPTY_TAGS;
        } else if (tags instanceof SortedMap) {
            this.tags = Collections.unmodifiableSortedMap((SortedMap<String, String>) tags);
        } else {
            this.tags = Collections.unmodifiableSortedMap(new TreeMap<>(tags));
        }
    }

    public String getKey() {
        return key;
    }

    /**
     * Returns the tags, sorted by key.
     *
     * @return the tags (immutable), sorted by key.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Build the MetricName that is this with another path appended to it.
     * The new MetricName inherits the tags of this one.
     *
     * @param parts The extra path elements to add to the new metric.
     * @return A new metric name relative to the original by the path specified
     * in parts.
     */
    public MetricName resolve(String... parts) {
        if (parts == null || parts.length == 0) {
            return this;
        }

        String newKey = Stream.concat(Stream.of(key), Stream.of(parts))
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(SEPARATOR));
        return new MetricName(newKey, tags);
    }

    /**
     * Add tags to a metric name and return the newly created MetricName.
     *
     * @param add Tags to add.
     * @return A newly created metric name with the specified tags associated with it.
     */
    public MetricName tagged(Map<String, String> add) {
        if (add == null) {
            return this;
        }
        final SortedMap<String, String> newTags = new TreeMap<>(tags);
        newTags.putAll(add);
        return new MetricName(key, newTags);
    }

    /**
     * Same as {@link #tagged(Map)}, but takes a variadic list of arguments.
     *
     * @param pairs An even list of strings acting as key-value pairs.
     * @return A newly created metric name with the specified tags associated with it.
     * @see #tagged(Map)
     */
    public MetricName tagged(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return this;
        }

        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Argument count must be even");
        }

        final SortedMap<String, String> newTags = new TreeMap<>(tags);
        for (int i = 0; i < pairs.length; i += 2) {
            newTags.put(pairs[i], pairs[i + 1]);
        }

        return new MetricName(key, newTags);
    }

    /**
     * Build the MetricName that is this with another path and tags appended to it.
     *
     * <p>
     * Semantically equivalent to: <br>
     * <code>this.resolve(append.getKey()).tagged(append.getTags());</code>
     *
     * @param append The extra name element to add to the new metric.
     * @return A new metric name with path appended to the original,
     * and tags included from both names.
     */
    public MetricName append(MetricName append) {
        return resolve(append.key).tagged(append.tags);
    }

    /**
     * Build a new metric name using the specific path components.
     *
     * <p>
     * Equivalent to:<br>
     * <code>MetricName.empty().resolve(parts);</code>
     *
     * @param parts Path of the new metric name.
     * @return A newly created metric name with the specified path.
     **/
    public static MetricName build(String... parts) {
        return EMPTY.resolve(parts);
    }

    @Override
    public String toString() {
        if (tags.isEmpty()) {
            return escapeKey(key);
        }

        StringBuilder builder = new StringBuilder();
        if (!key.isEmpty()) {
            builder.append(escapeKey(key));
        }

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            builder.append(',');
            builder.append(escapeTag(entry.getKey()));
            builder.append('=');
            builder.append(escapeTag(entry.getValue()));
        }

        return builder.toString();
    }

    /**
     * Parse a metric name from its string representation.
     * <p>
     * Format: "measurement,tag1=value1,tag2=value2" or just "measurement"
     *
     * @param metricNameString the string representation of the metric name
     * @return the parsed MetricName
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public static MetricName parse(String metricNameString) {
        if (metricNameString == null || metricNameString.isEmpty()) {
            return EMPTY;
        }

        int firstCommaIndex = findFirstUnescapedComma(metricNameString);

        if (firstCommaIndex == -1) {
            // No tags, just the key
            return new MetricName(unescapeKey(metricNameString), EMPTY_TAGS);
        }

        String keyPart = metricNameString.substring(0, firstCommaIndex);
        String tagsPart = metricNameString.substring(firstCommaIndex + 1);

        String key = unescapeKey(keyPart);
        SortedMap<String, String> tags = parseTags(tagsPart);

        return new MetricName(key, tags);
    }

    private static int findFirstUnescapedComma(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ',' && !isEscaped(str, i)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isEscaped(String str, int index) {
        if (index == 0) {
            return false;
        }
        int backslashCount = 0;
        for (int i = index - 1; i >= 0 && str.charAt(i) == '\\'; i--) {
            backslashCount++;
        }
        return backslashCount % 2 == 1;
    }

    private static SortedMap<String, String> parseTags(String tagsPart) {
        SortedMap<String, String> tags = new TreeMap<>();

        if (tagsPart.isEmpty()) {
            return tags;
        }

        // Check for trailing comma
        if (tagsPart.endsWith(",")) {
            throw new IllegalArgumentException("Trailing comma in tags");
        }

        // Check for consecutive commas
        if (tagsPart.contains(",,")) {
            throw new IllegalArgumentException("Consecutive commas in tags");
        }

        int start = 0;
        while (start < tagsPart.length()) {
            int commaIndex = findNextUnescapedComma(tagsPart, start);
            int endIndex = commaIndex == -1 ? tagsPart.length() : commaIndex;

            String tagPair = tagsPart.substring(start, endIndex);
            parseTagPair(tagPair, tags);

            start = endIndex + 1;
        }

        return tags;
    }

    private static int findNextUnescapedComma(String str, int startFrom) {
        for (int i = startFrom; i < str.length(); i++) {
            if (str.charAt(i) == ',' && !isEscaped(str, i)) {
                return i;
            }
        }
        return -1;
    }

    private static void parseTagPair(String tagPair, Map<String, String> tags) {
        if (tagPair.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty tag pair");
        }

        int equalIndex = findFirstUnescapedEquals(tagPair);
        if (equalIndex == -1) {
            throw new IllegalArgumentException("Invalid tag format: " + tagPair);
        }

        if (equalIndex == 0) {
            throw new IllegalArgumentException("Empty tag key in: " + tagPair);
        }

        String key = unescapeTag(tagPair.substring(0, equalIndex));
        String value = unescapeTag(tagPair.substring(equalIndex + 1));

        if (key.isEmpty()) {
            throw new IllegalArgumentException("Empty tag key after unescaping in: " + tagPair);
        }

        tags.put(key, value);
    }

    private static int findFirstUnescapedEquals(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '=' && !isEscaped(str, i)) {
                return i;
            }
        }
        return -1;
    }

    private static String escapeKey(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }
        StringBuilder escaped = new StringBuilder(key.length());
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            switch (c) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case ',':
                    escaped.append("\\,");
                    break;
                case '=':
                    escaped.append("\\=");
                    break;
                case ' ':
                    escaped.append("\\ ");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                default:
                    if (c < 32 || c == 127) { // Control characters and DEL
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private static String escapeTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            return tag;
        }
        StringBuilder escaped = new StringBuilder(tag.length());
        for (int i = 0; i < tag.length(); i++) {
            char c = tag.charAt(i);
            switch (c) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case ',':
                    escaped.append("\\,");
                    break;
                case '=':
                    escaped.append("\\=");
                    break;
                case ' ':
                    escaped.append("\\ ");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                default:
                    if (c < 32 || c == 127) { // Control characters and DEL
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private static String unescapeKey(String escapedKey) {
        if (escapedKey == null || escapedKey.trim().isEmpty()) {
            return escapedKey;
        }
        StringBuilder unescaped = new StringBuilder(escapedKey.length());
        for (int i = 0; i < escapedKey.length(); i++) {
            char c = escapedKey.charAt(i);
            if (c == '\\' && i + 1 < escapedKey.length()) {
                char next = escapedKey.charAt(i + 1);
                switch (next) {
                    case '\\':
                        unescaped.append('\\');
                        i++;
                        break;
                    case ',':
                        unescaped.append(',');
                        i++;
                        break;
                    case '=':
                        unescaped.append('=');
                        i++;
                        break;
                    case ' ':
                        unescaped.append(' ');
                        i++;
                        break;
                    case 'n':
                        unescaped.append('\n');
                        i++;
                        break;
                    case 't':
                        unescaped.append('\t');
                        i++;
                        break;
                    case 'r':
                        unescaped.append('\r');
                        i++;
                        break;
                    case 'u':
                        if (i + 6 <= escapedKey.length()) {
                            String hex = escapedKey.substring(i + 2, i + 6); // remove "\\u" prefix
                            try {
                                int codePoint = Integer.parseInt(hex, 16);
                                unescaped.append((char) codePoint);
                                i += 5; // 'u' + 4 hex digits
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid unicode escape sequence: \\u" + hex);
                            }
                        } else {
                            throw new IllegalArgumentException("Incomplete unicode escape sequence at end of string");
                        }
                        break;
                    default:
                        unescaped.append(c);
                        break;
                }
            } else if (c == '\\') {
                throw new IllegalArgumentException("Incomplete escape sequence at end of string");
            } else {
                unescaped.append(c);
            }
        }
        return unescaped.toString();
    }

    private static String unescapeTag(String escapedTag) {
        if (escapedTag == null || escapedTag.trim().isEmpty()) {
            return escapedTag;
        }
        StringBuilder unescaped = new StringBuilder(escapedTag.length());
        for (int i = 0; i < escapedTag.length(); i++) {
            char c = escapedTag.charAt(i);
            if (c == '\\' && i + 1 < escapedTag.length()) {
                char next = escapedTag.charAt(i + 1);
                switch (next) {
                    case '\\':
                        unescaped.append('\\');
                        i++;
                        break;
                    case ',':
                        unescaped.append(',');
                        i++;
                        break;
                    case '=':
                        unescaped.append('=');
                        i++;
                        break;
                    case ' ':
                        unescaped.append(' ');
                        i++;
                        break;
                    case 'n':
                        unescaped.append('\n');
                        i++;
                        break;
                    case 't':
                        unescaped.append('\t');
                        i++;
                        break;
                    case 'r':
                        unescaped.append('\r');
                        i++;
                        break;
                    case 'u':
                        if (i + 6 <= escapedTag.length()) {
                            String hex = escapedTag.substring(i + 2, i + 6); // remove "\\u" prefix
                            try {
                                int codePoint = Integer.parseInt(hex, 16);
                                unescaped.append((char) codePoint);
                                i += 5; // 'u' + 4 hex digits
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid unicode escape sequence: \\u" + hex);
                            }
                        } else {
                            throw new IllegalArgumentException("Incomplete unicode escape sequence at end of string");
                        }
                        break;
                    default:
                        unescaped.append(c);
                        break;
                }
            } else if (c == '\\') {
                throw new IllegalArgumentException("Incomplete escape sequence at end of string");
            } else {
                unescaped.append(c);
            }
        }
        return unescaped.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetricName)) {
            return false;
        }
        MetricName that = (MetricName) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, tags);
    }

    @Override
    public int compareTo(MetricName o) {
        int c = key.compareTo(o.getKey());
        if (c != 0) {
            return c;
        }

        return compareTags(tags, o.getTags());
    }

    private int compareTags(Map<String, String> left, Map<String, String> right) {
        Iterator<Map.Entry<String, String>> lit = left.entrySet().iterator();
        Iterator<Map.Entry<String, String>> rit = right.entrySet().iterator();

        while (lit.hasNext() && rit.hasNext()) {
            Map.Entry<String, String> l = lit.next();
            Map.Entry<String, String> r = rit.next();
            int c = l.getKey().compareTo(r.getKey());
            if (c != 0) {
                return c;
            }
            if (l.getValue() == null && r.getValue() == null) {
                return 0;
            } else if (l.getValue() == null) {
                return -1;
            } else if (r.getValue() == null) {
                return 1;
            } else {
                c = l.getValue().compareTo(r.getValue());
            }
            if (c != 0) {
                return c;
            }
        }
        if (lit.hasNext()) {
            return 1;
        } else if (rit.hasNext()) {
            return -1;
        } else {
            return 0;
        }
    }
}
