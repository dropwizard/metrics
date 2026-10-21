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

    @Test
    void quotesTagValuesThatCannotBeUnquoted() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("timer", "com.domain", MetricName.build("requests").tagged("env", "a,b", "note", "say \"hi\""));
        assertThatCode(() -> new ObjectName(on.toString())).doesNotThrowAnyException();
        assertThat(on.getKeyProperty("env")).isEqualTo(ObjectName.quote("a,b"));
        assertThat(on.getKeyProperty("note")).isEqualTo(ObjectName.quote("say \"hi\""));
        assertThat(on.getKeyProperty("name")).isEqualTo("requests");
        assertThat(on.getKeyProperty("type")).isEqualTo("timer");
    }

    @Test
    void quotesTagValuesThatArePatterns() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("timer", "com.domain", MetricName.build("requests").tagged("glob", "a*b"));
        assertThatCode(() -> new ObjectName(on.toString())).doesNotThrowAnyException();
        assertThat(on.getKeyProperty("glob")).isEqualTo(ObjectName.quote("a*b"));
        assertThat(on.isPropertyValuePattern("glob")).isFalse();
    }

    @Test
    void illegalTagKeyDoesNotDropOtherTags() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("timer", "com.domain", MetricName.build("requests").tagged("a=b", "x", "env", "prod"));
        assertThatCode(() -> new ObjectName(on.toString())).doesNotThrowAnyException();
        assertThat(on.getKeyProperty("env")).isEqualTo("prod");
        assertThat(on.getKeyProperty("name")).isEqualTo("requests");
        assertThat(on.getKeyPropertyList().containsKey("a=b")).isFalse();
    }

    @Test
    void tagsDoNotOverrideNameOrType() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("timer", "com.domain", MetricName.build("requests").tagged("name", "hijacked", "type", "hijacked"));
        assertThat(on.getKeyProperty("name")).isEqualTo("requests");
        assertThat(on.getKeyProperty("type")).isEqualTo("timer");
    }

    @Test
    void quotesTagValuesContainingNewlines() {
        ObjectName on = new DefaultObjectNameFactory().createName("timer", "com.domain",
                MetricName.build("requests").tagged("note", "line1\nline2"));
        assertThat(on.getKeyProperty("note")).isEqualTo(ObjectName.quote("line1\nline2"));
    }

    @Test
    void acceptsDoubleQuoteInTagKey() {
        ObjectName on = new DefaultObjectNameFactory().createName("timer", "com.domain",
                MetricName.build("requests").tagged("a\"b", "value"));
        assertThat(on.getKeyProperty("a\"b")).isEqualTo("value");
    }

    @Test
    void acceptsCarriageReturnInTagKey() {
        ObjectName on = new DefaultObjectNameFactory().createName("timer", "com.domain",
                MetricName.build("requests").tagged("a\rb", "value"));
        assertThat(on.getKeyProperty("a\rb")).isEqualTo("value");
    }
}
