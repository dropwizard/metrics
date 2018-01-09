package com.codahale.metrics.jmx;

import com.codahale.metrics.MetricName;

import javax.management.ObjectName;

public interface ObjectNameFactory {

    ObjectName createName(String type, String domain, MetricName name);
}
