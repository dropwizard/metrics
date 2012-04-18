package com.yammer.metrics.spring;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:metered-class.xml")
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class MeteredClassTest {

	@Autowired
	MeteredClass meteredClass;

	@Autowired
	MetricsRegistry metricsRegistry;

	Gauge<Object> gaugedField;
	Gauge<Object> gaugedMethod;
	Timer timedMethod;
	Meter meteredMethod;
	Meter exceptionMeteredMethod;
	Timer triple_Timed;
	Meter triple_Metered;
	Meter triple_ExceptionMetered;
	
	@Before
	@SuppressWarnings(value = "unchecked")
	public void init() {
		Map<MetricName, Metric> metrics = metricsRegistry.getAllMetrics();

		gaugedField = (Gauge<Object>) metrics.get(new MetricName(MeteredClass.class, "gaugedField"));
		gaugedMethod = (Gauge<Object>) metrics.get(new MetricName(MeteredClass.class, "gaugedMethod"));
		timedMethod = (Timer) metrics.get(new MetricName(MeteredClass.class, "timedMethod"));
		meteredMethod = (Meter) metrics.get(new MetricName(MeteredClass.class, "meteredMethod"));
		exceptionMeteredMethod = (Meter) metrics.get(new MetricName(MeteredClass.class, "exceptionMeteredMethodExceptions"));
		triple_Timed = (Timer) metrics.get(new MetricName(MeteredClass.class, "triplyMeteredMethod-timed"));
		triple_Metered = (Meter) metrics.get(new MetricName(MeteredClass.class, "triplyMeteredMethod-metered"));
		triple_ExceptionMetered = (Meter) metrics.get(new MetricName(MeteredClass.class, "triplyMeteredMethod-exceptionMetered"));
	}

	@Test
	public void gauges() {
		assertEquals(999, gaugedField.getValue());
		assertEquals(999, gaugedMethod.getValue());

		meteredClass.setGaugedField(1000);

		assertEquals(1000, gaugedField.getValue());
		assertEquals(1000, gaugedMethod.getValue());
	}

	@Test
	public void timedMethod() throws Throwable {
		assertEquals(0, timedMethod.getCount());

		meteredClass.timedMethod(false);
		assertEquals(1, timedMethod.getCount());

		// count increments even when the method throws an exception
		try {
			meteredClass.timedMethod(true);
			fail();
		} catch (Throwable e) {
			assertTrue(e instanceof BogusException);
		}
		assertEquals(2, timedMethod.getCount());
	}

	@Test
	public void meteredMethod() throws Throwable {
		assertEquals(0, meteredMethod.getCount());

		meteredClass.meteredMethod();
		assertEquals(1, meteredMethod.getCount());
	}

	@Test
	public void exceptionMeteredMethod() throws Throwable {
		assertEquals(0, exceptionMeteredMethod.getCount());

		// doesn't throw an exception
		meteredClass.exceptionMeteredMethod(null);
		assertEquals(0, exceptionMeteredMethod.getCount());

		// throws the wrong exception
		try {
			meteredClass.exceptionMeteredMethod(RuntimeException.class);
			fail();
		} catch (Throwable t) {
			assertTrue(t instanceof RuntimeException);
		}
		assertEquals(0, exceptionMeteredMethod.getCount());

		// throws the right exception
		try {
			meteredClass.exceptionMeteredMethod(BogusException.class);
			fail();
		} catch (Throwable t) {
			assertTrue(t instanceof BogusException);
		}
		assertEquals(1, exceptionMeteredMethod.getCount());
	}

	@Test
	public void triplyMeteredMethod() throws Throwable {
		assertEquals(0, triple_Metered.getCount());
		assertEquals(0, triple_Timed.getCount());
		assertEquals(0, triple_ExceptionMetered.getCount());

		// doesn't throw an exception
		meteredClass.triplyMeteredMethod(false);
		assertEquals(1, triple_Metered.getCount());
		assertEquals(1, triple_Timed.getCount());
		assertEquals(0, triple_ExceptionMetered.getCount());

		// throws an exception
		try {
			meteredClass.triplyMeteredMethod(true);
			fail();
		} catch (Throwable t) {
			assertTrue(t instanceof BogusException);
		}
		assertEquals(2, triple_Metered.getCount());
		assertEquals(2, triple_Timed.getCount());
		assertEquals(1, triple_ExceptionMetered.getCount());
	}
}
