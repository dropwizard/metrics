package com.yammer.metrics.core;

import com.yammer.metrics.annotation.ExceptionMetered;

import java.lang.reflect.Method;

/**
 * A value class encapsulating a metric's owning class and name.
 */
public class MetricName implements Comparable<MetricName> {
    private final String domain;
    private final String type;
    private final String name;
    private final String scope;

    /**
     * Creates a new {@link MetricName} without a scope.
     *
     * @param klass the {@link Class} to which the {@link Metric} belongs
     * @param name  the name of the {@link Metric}
     */
    public MetricName(Class<?> klass, String name) {
        this(klass, name, null);
    }

    /**
     * Creates a new {@link MetricName} without a scope.
     *
     * @param domain the domain to which the {@link Metric} belongs
     * @param type  the type to which the {@link Metric} belongs
     * @param name  the name of the {@link Metric}
     */
    public MetricName(String domain, String type, String name) {
        this(domain, type, name, null);
    }

    /**
     * Creates a new {@link MetricName} without a scope.
     *
     * @param klass the {@link Class} to which the {@link Metric} belongs
     * @param name  the name of the {@link Metric}
     * @param scope the scope of the {@link Metric}
     */
    public MetricName(Class<?> klass, String name, String scope) {
        this(getPackageName(klass),
             getClassName(klass),
             name,
             scope);
    }

    /**
     * Creates a new {@link MetricName} without a scope.
     *
     * @param domain the domain to which the {@link Metric} belongs
     * @param type  the type to which the {@link Metric} belongs
     * @param name  the name of the {@link Metric}
     * @param scope the scope of the {@link Metric}
     */
    public MetricName(String domain, String type, String name, String scope) {
        if (domain == null || type == null) {
            throw new IllegalArgumentException("Both domain and type need to be specified");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name needs to be specified");
        }
        this.domain = domain;
        this.type = type;
        this.name = name;
        this.scope = scope;
    }

    /**
     * Returns the domain to which the {@link Metric} belongs. For class-based metrics, this will be
     * the package name of the {@link Class} to which the {@link Metric} belongs.
     *
     * @return the domain to which the {@link Metric} belongs
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns the type to which the {@link Metric} belongs. For class-based metrics, this will be
     * the simple class name of the {@link Class} to which the {@link Metric} belongs.
     *
     * @return the type to which the {@link Metric} belongs
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the name of the {@link Metric}.
     *
     * @return the name of the {@link Metric}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the scope of the {@link Metric}.
     *
     * @return the scope of the {@link Metric}
     */
    public String getScope() {
        return scope;
    }

    /**
     * Returns {@code true} if the {@link Metric} has a scope, {@code false} otherwise.
     *
     * @return {@code true} if the {@link Metric} has a scope
     */
    public boolean hasScope() {
        return scope != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final MetricName that = (MetricName) o;
        return domain.equals(that.domain) &&
                name.equals(that.name) &&
                type.equals(that.type) &&
                (scope == null ? that.scope == null : scope.equals(that.scope));
    }

    @Override
    public int hashCode() {
        int result = domain.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return domain + '.' + type + '.' + name + (scope == null ? "" : '.' + scope);
    }

    @Override
    public int compareTo(MetricName o) {
        int result = domain.compareTo(o.domain);
        if (result != 0) {
            return result;
        }

        result = type.compareTo(o.type);
        if (result != 0) {
            return result;
        }

        result = name.compareTo(o.name);
        if (result != 0) {
            return result;
        }

        if (scope == null) {
            if (o.scope != null) {
                return -1;
            }
            return 0;
        }

        if (o.scope != null) {
            return scope.compareTo(o.scope);
        }
        return 1;
    }

    private static String getPackageName(Class<?> klass) {
        return klass.getPackage() == null ? "" : klass.getPackage().getName();
    }

    private static String getClassName(Class<?> klass) {
        return klass.getSimpleName().replaceAll("\\$$", "");
    }

    private static String chooseDomain(String domain, Class<?> klass) {
        if(domain == null || domain.isEmpty()) {
            domain = getPackageName(klass);
        }
        return domain;
    }

    private static String chooseType(String type, Class<?> klass) {
        if(type == null || type.isEmpty()) {
            type = getClassName(klass);
        }
        return type;
    }

    private static String chooseName(String name, Method method) {
        if(name == null || name.isEmpty()) {
            name = method.getName();
        }
        return name;
    }

    public static MetricName forTimedMethod(Class<?> klass, Method method, com.yammer.metrics.annotation.Timed annotation) {
        return new MetricName(chooseDomain(annotation.group(), klass),
                              chooseType(annotation.type(), klass),
                              chooseName(annotation.name(), method));
    }

    public static MetricName forMeteredMethod(Class<?> klass, Method method, com.yammer.metrics.annotation.Metered annotation) {
        return new MetricName(chooseDomain(annotation.group(), klass),
                              chooseType(annotation.type(), klass),
                              chooseName(annotation.name(), method));
    }

    public static MetricName forGaugeMethod(Class<?> klass, Method method, com.yammer.metrics.annotation.Gauge annotation) {
        return new MetricName(chooseDomain(annotation.group(), klass),
                              chooseType(annotation.type(), klass),
                              chooseName(annotation.name(), method));
    }

    public static MetricName forExceptionMeteredMethod(Class<?> klass, Method method, com.yammer.metrics.annotation.ExceptionMetered annotation) {
        return new MetricName(chooseDomain(annotation.group(), klass),
                              chooseType(annotation.type(), klass),
                              annotation.name() == null || annotation.name().isEmpty() ?
                                      method.getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX :
                                      annotation.name());
    }
}
