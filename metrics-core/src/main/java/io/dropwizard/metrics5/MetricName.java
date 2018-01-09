package io.dropwizard.metrics5;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * A metric name with the ability to include semantic tags.
 * This replaces the previous style where metric names where strictly dot-separated strings.
 */
public class MetricName implements Comparable<MetricName> {

    private static final String SEPARATOR = ".";
    private static final Map<String, String> EMPTY_TAGS = Collections.unmodifiableMap(new HashMap<String, String>());
    static final MetricName EMPTY = new MetricName("");

    private final String key;
    private final Map<String, String> tags;

    private MetricName(String key) {
        this(key, EMPTY_TAGS);
    }

    public MetricName(String key, Map<String, String> tags) {
        this.key = Objects.requireNonNull(key);
        this.tags = tags.isEmpty() ? EMPTY_TAGS : Collections.unmodifiableMap(tags);
    }

    public String getKey() {
        return key;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Build the MetricName that is this with another path appended to it.
     * The new MetricName inherits the tags of this one.
     *
     * @param p The extra path element to add to the new metric.
     * @return A new metric name relative to the original by the path specified
     * in p.
     */
    public MetricName resolve(String p) {
        final String next;
        if (p != null && !p.isEmpty()) {
            if (!key.isEmpty()) {
                next = key + SEPARATOR + p;
            } else {
                next = p;
            }
        } else {
            next = key;
        }

        return new MetricName(next, tags);
    }

    /**
     * Add tags to a metric name and return the newly created MetricName.
     *
     * @param add Tags to add.
     * @return A newly created metric name with the specified tags associated with it.
     */
    public MetricName tagged(Map<String, String> add) {
        final Map<String, String> newTags = new HashMap<>(add);
        newTags.putAll(tags);
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
     * Join the specified set of metric names.
     *
     * @param parts Multiple metric names to join using the separator.
     * @return A newly created metric name which has the name of the specified
     * parts and includes all tags of all child metric names.
     **/
    public static MetricName join(MetricName... parts) {
        final StringBuilder nameBuilder = new StringBuilder();
        final Map<String, String> tags = new HashMap<>();

        boolean first = true;

        for (MetricName part : parts) {
            final String name = part.getKey();
            if (name != null && !name.isEmpty()) {
                if (first) {
                    first = false;
                } else {
                    nameBuilder.append(SEPARATOR);
                }

                nameBuilder.append(name);
            }

            if (!part.getTags().isEmpty()) {
                tags.putAll(part.getTags());
            }
        }

        return new MetricName(nameBuilder.toString(), tags);
    }

    /**
     * Build a new metric name using the specific path components.
     *
     * @param parts Path of the new metric name.
     * @return A newly created metric name with the specified path.
     **/
    public static MetricName build(String... parts) {
        if (parts == null || parts.length == 0) {
            return MetricName.EMPTY;
        } else if (parts.length == 1) {
            return new MetricName(parts[0], EMPTY_TAGS);
        }
        return new MetricName(buildName(parts), EMPTY_TAGS);
    }

    private static String buildName(String... names) {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;

        for (String name : names) {
            if (name == null || name.isEmpty()) {
                continue;
            }

            if (first) {
                first = false;
            } else {
                builder.append(SEPARATOR);
            }

            builder.append(name);
        }

        return builder.toString();
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
        if (o == null) {
            return -1;
        }

        int c = compareName(key, o.getKey());
        if (c != 0) {
            return c;
        }

        return compareTags(tags, o.getTags());
    }

    private int compareName(String left, String right) {
        if (left.isEmpty() && right.isEmpty()) {
            return 0;
        } else if (left.isEmpty()) {
            return 1;
        } else if (right.isEmpty()) {
            return -1;
        }

        return left.compareTo(right);
    }

    private int compareTags(Map<String, String> left, Map<String, String> right) {
        if (left.isEmpty() && right.isEmpty()) {
            return 0;
        } else if (left.isEmpty()) {
            return 1;
        } else if (right.isEmpty()) {
            return -1;
        }
        final Iterable<String> keys = uniqueSortedKeys(left, right);
        for (final String key : keys) {
            final String a = left.get(key);
            final String b = right.get(key);
            if (a == null && b == null) {
                continue;
            } else if (a == null) {
                return -1;
            } else if (b == null) {
                return 1;
            }
            int c = a.compareTo(b);
            if (c != 0) {
                return c;
            }
        }

        return 0;
    }

    private Iterable<String> uniqueSortedKeys(Map<String, String> left, Map<String, String> right) {
        final Set<String> set = new TreeSet<>(left.keySet());
        set.addAll(right.keySet());
        return set;
    }
}
