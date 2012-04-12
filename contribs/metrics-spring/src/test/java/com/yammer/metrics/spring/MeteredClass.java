package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

public class MeteredClass {

	@Gauge
	private int gaugedField = 999;

	@Gauge
	public int gaugedMethod() {
		return gaugedField;
	}

	public void setGaugedField(int value) {
		this.gaugedField = value;
	}

	@Timed
	public void timedMethod(boolean doThrow) throws Throwable {
		if (doThrow) throw new BogusException();
	}

	@Metered
	public void meteredMethod() {}

	@ExceptionMetered(cause=BogusException.class)
	public <T extends Throwable> void exceptionMeteredMethod(Class<T> clazz) throws Throwable {
		if (clazz != null) throw clazz.newInstance();
	}

	@Timed(name="triplyMeteredMethod-timed")
	@Metered(name="triplyMeteredMethod-metered")
	@ExceptionMetered(name="triplyMeteredMethod-exceptionMetered", cause=BogusException.class)
	public void triplyMeteredMethod(boolean doThrow) throws Throwable {
		if (doThrow) throw new BogusException();
	}

}