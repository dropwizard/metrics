package com.yammer.metrics.core;

import javax.management.ObjectName;


/**
 * A value class encapsulating a metric's owning class and name.
 */
public class MetricName implements Comparable<MetricName> {
    private final String group;
    private final String type;
    private final String name;
    private final String scope;
    private final String fqn;
    private final ObjectName mBeanName;

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
     * @param group the group to which the {@link Metric} belongs
     * @param type  the type to which the {@link Metric} belongs
     * @param name  the name of the {@link Metric}
     */
    public MetricName(String group, String type, String name) {
        this(group, type, name, null);
    }

    /**
     * Creates a new {@link MetricName} without a scope.
     *
     * @param klass the {@link Class} to which the {@link Metric} belongs
     * @param name  the name of the {@link Metric}
     * @param scope the scope of the {@link Metric}
     */
    public MetricName(Class<?> klass, String name, String scope) {
        this(klass.getPackage() == null ? "" : klass.getPackage().getName(),
             klass.getSimpleName().replaceAll("\\$$", ""),
             name,
             scope);
    }

    /**
     * Creates a new {@link MetricName} without a scope.
     *
     * @param group the group to which the {@link Metric} belongs
     * @param type  the type to which the {@link Metric} belongs
     * @param name  the name of the {@link Metric}
     * @param scope the scope of the {@link Metric}
     */
    public MetricName(String group, String type, String name, String scope) {
        this(group, type, name, scope, createMBeanName(group, type, name, scope));
    }

    /**
     * Creates a new {@link MetricName} without a scope.
     *
     * @param group     the group to which the {@link Metric} belongs
     * @param type      the type to which the {@link Metric} belongs
     * @param name      the name of the {@link Metric}
     * @param scope     the scope of the {@link Metric}
     * @param mBeanName the 'ObjectName', represented as a string, to use when registering the
     *                  MBean.
     */
    public MetricName(String group, String type, String name, String scope, ObjectName mBeanName) {
        if (group == null || type == null) {
            throw new IllegalArgumentException("Both group and type need to be specified");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name needs to be specified");
        }
        this.mBeanName = ObjectName.getInstance(mBeanName);
        this.group = group;
        this.type = type;
        this.name = name;
        this.scope = scope;
        this.fqn = mBeanName.toString();
    }

    /**
     * Returns the group to which the {@link Metric} belongs. For class-based metrics, this will be
     * the package name of the {@link Class} to which the {@link Metric} belongs.
     *
     * @return the group to which the {@link Metric} belongs
     */
    public String getGroup() {
        return group;
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

    /**
     * Returns the MBean name for the {@link Metric} identified by this metric name.
     *
     * @return the MBean name
     */
    public ObjectName getMBeanName() {
        return mBeanName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final MetricName that = (MetricName) o;
        return fqn.equals(that.fqn);
    }

    @Override
    public int hashCode() {
        return fqn.hashCode();
    }

    @Override
    public String toString() {
        return fqn;
    }

    private static ObjectName createMBeanName(String group, String type, String name, String scope) {
        final StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(group);
        nameBuilder.append(":type=");
        nameBuilder.append(type);
        if (scope != null) {
            nameBuilder.append(",scope=");
            nameBuilder.append(scope);
        }
        if (name.length() > 0) {
            nameBuilder.append(",name=");
            nameBuilder.append(name);
        }
        try {
            return ObjectName.getInstance(nameBuilder.toString());
        }
        catch(Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public int compareTo(MetricName o) {
        return fqn.compareTo(o.fqn);
    }
}
