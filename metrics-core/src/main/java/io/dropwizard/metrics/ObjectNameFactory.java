package io.dropwizard.metrics;

import javax.management.ObjectName;

public interface ObjectNameFactory {

	ObjectName createName(String type, String domain, MetricName name);
}
