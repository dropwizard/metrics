package io.dropwizard.metrics5.jmx;

import io.dropwizard.metrics5.MetricName;
import org.junit.jupiter.api.Test;

import javax.management.ObjectName;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultObjectNameFactoryTest {

    @Test
    void createsObjectNameWithDomainInInput() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("type", "com.domain", MetricName.build("something.with.dots").tagged("foo", "bar", "baz", "biz"));
        assertThat(on.getDomain()).isEqualTo("com.domain");
        assertThat(on.getKeyProperty("foo")).isEqualTo("bar");
        assertThat(on.getKeyProperty("baz")).isEqualTo("biz");
    }

    @Test
    void createsObjectNameWithNameAsKeyPropertyName() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("type", "com.domain", MetricName.build("something.with.dots").tagged("foo", "bar", "baz", "biz"));
        assertThat(on.getKeyProperty("name")).isEqualTo("something.with.dots");
        assertThat(on.getKeyProperty("foo")).isEqualTo("bar");
        assertThat(on.getKeyProperty("baz")).isEqualTo("biz");

    }

    @Test
    void createsObjectNameWithNameWithDisallowedUnquotedCharacters() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("type", "com.domain", MetricName.build("something.with.quotes(\"ABcd\")").tagged("foo", "bar", "baz", "biz"));
        assertThatCode(() -> new ObjectName(on.toString())).doesNotThrowAnyException();
        assertThat(on.getKeyProperty("name")).isEqualTo("\"something.with.quotes(\\\"ABcd\\\")\"");
        assertThat(on.getKeyProperty("foo")).isEqualTo("bar");
        assertThat(on.getKeyProperty("baz")).isEqualTo("biz");
    }
}
