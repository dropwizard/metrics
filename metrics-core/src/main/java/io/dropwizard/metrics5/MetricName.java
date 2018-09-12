package io.dropwizard.metrics5;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A metric name with the ability to include semantic tags.
 * This replaces the previous style where metric names where strictly dot-separated strings.
 */
public class MetricName implements Comparable<MetricName> {

    private static final String SEPARATOR = ".";
    private static final Map<String, String> EMPTY_TAGS = Collections.emptyMap();
    static final MetricName EMPTY = new MetricName("", EMPTY_TAGS);

    /**
     * Returns an empty metric name.
     * @return an empty metric name.
     */
    public static MetricName empty() {
        return EMPTY;
    }

    private final String key;
    private final Map<String, String> tags;

    public MetricName(String key, Map<String, String> tags) {
        this.key = Objects.requireNonNull(key);
        this.tags = tags.isEmpty() ? EMPTY_TAGS : unmodifiableSortedCopy(tags);
    }

    public String getKey() {
        return key;
    }

    /**
     * Returns the tags, sorted by key.
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
        final Map<String, String> newTags = new HashMap<>();
        newTags.putAll(tags);
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

        final Map<String, String> add = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            add.put(pairs[i], pairs[i + 1]);
        }

        return tagged(add);
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
        return tags.isEmpty() ? key : key + tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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

    private static <K extends Comparable<K>,V> Map<K, V> unmodifiableSortedCopy(Map<K, V> map) {
        LinkedHashMap<K, V> sorted = new LinkedHashMap<>();
        map.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return Collections.unmodifiableMap(sorted);
    }

}
