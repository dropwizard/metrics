package com.codahale.metrics.jvm;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.codahale.metrics.Gauge;

public class ProcessCpuLoadGauge implements Gauge<Double> {

	@Override
	public Double getValue() {		
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName objName = new ObjectName("java.lang:type=OperatingSystem");
			Object objValue = mbs.getAttribute(objName, "ProcessCpuLoad");
			if(objValue instanceof Double) {
				return ((Double)objValue).doubleValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return 0.0;
	}

}
