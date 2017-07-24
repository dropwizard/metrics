package com.codahale.metrics.jmx;

import javax.management.ObjectName;

public interface ObjectNameFactory {

    ObjectName createName(String type, String domain, String name);
}
