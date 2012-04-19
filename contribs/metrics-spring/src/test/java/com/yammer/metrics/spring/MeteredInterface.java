package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

public interface MeteredInterface {

	@Timed
	public boolean timedMethod();

	@Metered
	public boolean meteredMethod();

	@ExceptionMetered
	public void exceptionMeteredMethod() throws Throwable;

}