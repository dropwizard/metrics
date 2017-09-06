package io.dropwizard.metrics.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import io.dropwizard.metrics.MetricName;

public interface ObjectNameFactory {

	ObjectName createName(String type, String domain, MetricName name) throws MalformedObjectNameException;
}
