package com.codahale.metrics.jvm;

import java.lang.management.ManagementFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.codahale.metrics.Gauge;

public class ProcessCpuLoadGauge implements Gauge<Double> {

	@Override
	public Double getValue() {		
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName objName = new ObjectName("java.lang:type=OperatingSystem");
			Object objValue = mbs.getAttribute(objName, "ProcessCpuLoad");
			if(objValue instanceof Double) {
				return (Double)objValue;
			}
		} catch (MalformedObjectNameException | InstanceNotFoundException | AttributeNotFoundException | ReflectionException | MBeanException e) {
			e.printStackTrace();
		} 
		return 0.0;
	}

}
