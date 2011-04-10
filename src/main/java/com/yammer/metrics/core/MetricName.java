package com.yammer.metrics.core;

import javax.management.*;

/**
 * A value class encapsulating a metric's owning class and name.
 *
 * @author
 */
public class MetricName {
	private final String domain;
	private final String type;
	private final String name;

  public MetricName(Class<?> klass, String name) {
    this.domain = klass.getPackage().getName();
    this.type = klass.getSimpleName();
    this.name = name;
  }

	/**
	 * Creates a new {@link MetricName}.
	 *
	 * @param domain the domain of the {@link Metric}. Typically the containing
	 *               classes package name.
	 * @param type the type of the {@link Metric}. Typically the containing
	 *               classes simple name.
	 * @param name the name of the {@link Metric}
	 */
	public MetricName(String domain, String type, String name) {
	  this.domain = domain;
	  this.type = type;
	  this.name = name;
	}

	public String getDomain() {
	  return domain;
	}
	
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

  public ObjectName getObjectName() throws MalformedObjectNameException {
    return new ObjectName(
				String.format("%s:type=%s,name=%s", domain, type.replaceAll("\\$$", ""), name));
  }

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		final MetricName that = (MetricName) o;

		if (domain != null ? !domain.equals(that.domain) : that.domain != null) {
			return false;
		}
		if (type != null ? !type.equals(that.type) : that.type != null) {
			return false;
		}
		return !(name != null ? !name.equals(that.name) : that.name != null);

	}

	@Override
	public int hashCode() {
		int result = domain != null ? domain.hashCode() : 0;
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
