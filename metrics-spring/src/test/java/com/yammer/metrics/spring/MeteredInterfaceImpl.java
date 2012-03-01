package com.yammer.metrics.spring;

import org.springframework.stereotype.Component;

@Component
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

    public static class BogusException extends Throwable {}

}