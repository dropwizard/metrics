package com.codahale.metrics.jmx;

import org.junit.Test;

import javax.management.ObjectName;

import static org.assertj.core.api.Assertions.assertThat;

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
