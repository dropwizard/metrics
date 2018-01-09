package io.dropwizard.metrics5.jmx;

import io.dropwizard.metrics5.MetricName;

import javax.management.ObjectName;

public interface ObjectNameFactory {

    ObjectName createName(String type, String domain, MetricName name);
}
