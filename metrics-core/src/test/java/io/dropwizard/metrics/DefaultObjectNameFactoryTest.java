package io.dropwizard.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.Test;

import io.dropwizard.metrics.DefaultObjectNameFactory;
import io.dropwizard.metrics.MetricName;

public class DefaultObjectNameFactoryTest {

	@Test
	public void createsObjectNameWithDomainInInput() throws MalformedObjectNameException {
		DefaultObjectNameFactory f = new DefaultObjectNameFactory();
		ObjectName on = f.createName("type", "com.domain", MetricName.build("something.with.dots"));
		assertThat(on.getDomain()).isEqualTo("com.domain");
	}

	@Test
	public void createsObjectNameWithNameAsKeyPropertyName() throws MalformedObjectNameException {
		DefaultObjectNameFactory f = new DefaultObjectNameFactory();
		ObjectName on = f.createName("type", "com.domain", MetricName.build("something.with.dots"));
		assertThat(on.getKeyProperty("name")).isEqualTo("something.with.dots");
	}
}
