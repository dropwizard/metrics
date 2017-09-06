package io.dropwizard.metrics.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import io.dropwizard.metrics.MetricName;

public class DefaultObjectNameFactory implements ObjectNameFactory {

	@Override
	public ObjectName createName(String type, String domain, MetricName metricName) throws MalformedObjectNameException {
	    String name = metricName.getKey();
		try {
			ObjectName objectName = new ObjectName(domain, "name", name);
			if (objectName.isPattern()) {
				objectName = new ObjectName(domain, "name", ObjectName.quote(name));
			}
			return objectName;
		} catch (MalformedObjectNameException e) {
			return new ObjectName(domain, "name", ObjectName.quote(name));
		}
	}

}
