package com.codahale.metrics.jvm;

import org.junit.Assert;
import org.junit.Test;

public class ProcessCpuLoadGaugeTest {

	@Test
	public void test() {
		ProcessCpuLoadGauge gauge = new ProcessCpuLoadGauge();
		Assert.assertNotNull(gauge.getValue());
	}
	
}
