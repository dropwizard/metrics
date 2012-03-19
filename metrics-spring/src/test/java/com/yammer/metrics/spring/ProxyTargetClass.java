package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.Timed;

public class ProxyTargetClass implements UselessInterface {

	@Timed
	public void timed() {}

}