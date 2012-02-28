package com.yammer.metrics.spring;

import org.springframework.stereotype.Component;

import com.yammer.metrics.annotation.Timed;

@Component
public class ProxyTargetClass implements UselessInterface {

	@Timed
	public void timed() {}

}