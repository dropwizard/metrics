package com.codahale.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import javax.management.ObjectName;

import org.junit.Test;

public class DefaultObjectNameFactoryTest {

	@Test
	public void createsObjectNameWithDomainInInput() {
		DefaultObjectNameFactory f = new DefaultObjectNameFactory();
		ObjectName on = f.createName("type", "com.domain", "something.with.dots");
		assertThat(on.getDomain()).isEqualTo("com.domain");
	}

	@Test
	public void createsObjectNameWithNameAsKeyPropertyName() {
		DefaultObjectNameFactory f = new DefaultObjectNameFactory();
		ObjectName on = f.createName("type", "com.domain", "something.with.dots");
		assertThat(on.getKeyProperty("name")).isEqualTo("something.with.dots");
	}
}
