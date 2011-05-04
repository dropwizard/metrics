package com.yammer.metrics.core;

/**
 * A value class encapsulating a metric's owning class and name.
 */
public class MetricName {
	private final Class<?> klass;
	private final String name;

	/**
	 * Creates a new {@link MetricName}.
	 *
	 * @param klass the {@link Class} to which the {@link Metric} belongs
	 * @param name the name of the {@link Metric}
	 */
	public MetricName(Class<?> klass, String name) {
		this.klass = klass;
		this.name = name;
	}

	/**
	 * Returns the {@link Class} to which the {@link Metric} belongs.
	 *
	 * @return the {@link Class} to which the {@link Metric} belongs
	 */
	public Class<?> getKlass() {
		return klass;
	}

	/**
	 * Returns the name of the {@link Metric}.
	 *
	 * @return the name of the {@link Metric}
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		final MetricName that = (MetricName) o;

        return !(klass != null ? !klass.equals(that.klass) : that.klass != null)
                && !(name != null ? !name.equals(that.name) : that.name != null);

    }

	@Override
	public int hashCode() {
		int result = klass != null ? klass.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
