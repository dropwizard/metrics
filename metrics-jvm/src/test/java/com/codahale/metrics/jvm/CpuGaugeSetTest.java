package com.codahale.metrics.jvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CpuGaugeSetTest {

	@Test
	public void test() {
		CpuGaugeSet gauges = new CpuGaugeSet();
		assertEquals(1, gauges.getMetrics().size());
		assertTrue(gauges.getMetrics().containsKey("processCpuLoad"));
	}
	
}
