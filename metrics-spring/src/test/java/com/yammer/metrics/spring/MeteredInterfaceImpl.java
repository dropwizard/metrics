package com.yammer.metrics.spring;

public class MeteredInterfaceImpl implements MeteredInterface {

	@Override
	public boolean timedMethod() {
		return true;
	}

	@Override
	public boolean meteredMethod() {
		return true;
	}

	@Override
	public void exceptionMeteredMethod() throws Throwable {
		throw new BogusException();
	}

}