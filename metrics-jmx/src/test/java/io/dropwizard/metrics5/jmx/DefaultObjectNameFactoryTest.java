package io.dropwizard.metrics5.jmx;

import io.dropwizard.metrics5.MetricName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.management.ObjectName;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultObjectNameFactoryTest {

    @Test
    void createsObjectName() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("type", "com.domain", MetricName.build("something.with.dots").tagged("foo", "bar", "baz", "biz"));

        assertIsRegistrable(on);
        assertThat(on.getDomain()).isEqualTo("com.domain");
        assertThat(on.getKeyProperty("name")).isEqualTo("something.with.dots");
        assertThat(on.getKeyProperty("type")).isEqualTo("type");
        assertThat(on.getKeyProperty("foo")).isEqualTo("bar");
        assertThat(on.getKeyProperty("baz")).isEqualTo("biz");
    }

    @ParameterizedTest
    @MethodSource("unsafeNames")
    void handlesUnsafeCharactersInDomain(UnsafeName name) {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("count", name.raw, MetricName.build("name").tagged("foo", "bar", "baz", "biz"));

        assertIsRegistrable(on);
        assertThat(on.getDomain()).isEqualTo(name.asDomain);
        assertThat(on.getKeyProperty("type")).isEqualTo("count");
        assertThat(on.getKeyProperty("name")).isEqualTo("name");
        assertThat(on.getKeyProperty("foo")).isEqualTo("bar");
        assertThat(on.getKeyProperty("baz")).isEqualTo("biz");
    }

    @ParameterizedTest
    @MethodSource("unsafeNames")
    void handlesUnsafeCharactersInName(UnsafeName name) {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("count", "com.domain", MetricName.build(name.raw).tagged("foo", "bar", "baz", "biz"));

        assertIsRegistrable(on);
        assertThat(on.getDomain()).isEqualTo("com.domain");
        assertThat(on.getKeyProperty("type")).isEqualTo("count");
        assertThat(on.getKeyProperty("name")).isEqualTo(name.quoted);
        assertThat(on.getKeyProperty("foo")).isEqualTo("bar");
        assertThat(on.getKeyProperty("baz")).isEqualTo("biz");
    }

    @ParameterizedTest
    @MethodSource("unsafeNames")
    void handlesUnsafeCharactersInType(UnsafeName name) {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName(name.raw, "com.domain", MetricName.build("name").tagged("foo", "bar", "baz", "biz"));

        assertIsRegistrable(on);
        assertThat(on.getDomain()).isEqualTo("com.domain");
        assertThat(on.getKeyProperty("type")).isEqualTo(name.quoted);
        assertThat(on.getKeyProperty("name")).isEqualTo("name");
        assertThat(on.getKeyProperty("foo")).isEqualTo("bar");
        assertThat(on.getKeyProperty("baz")).isEqualTo("biz");
    }

    @ParameterizedTest
    @MethodSource("unsafeNames")
    void handlesUnsafeCharactersInTagValue(UnsafeName name) {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("count", "com.domain", MetricName.build("name").tagged("foo", name.raw));

        assertIsRegistrable(on);
        assertThat(on.getDomain()).isEqualTo("com.domain");
        assertThat(on.getKeyProperty("type")).isEqualTo("count");
        assertThat(on.getKeyProperty("name")).isEqualTo("name");
        assertThat(on.getKeyProperty("foo")).isEqualTo(name.quoted);
    }

    @ParameterizedTest
    @MethodSource("unsafeNames")
    void handlesUnsafeCharactersInTagName(UnsafeName name) {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("count", "com.domain", MetricName.build("name").tagged(name.raw, "bar"));

        assertIsRegistrable(on);
        assertThat(on.getDomain()).isEqualTo("com.domain");
        assertThat(on.getKeyProperty("type")).isEqualTo("count");
        assertThat(on.getKeyProperty("name")).isEqualTo("name");
        assertThat(on.getKeyProperty(name.asKey)).isEqualTo("bar");
    }

    @Test
    public void doesNotQuoteNamesThatAreValidUnquotedValues() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("type-.", "domain-.", MetricName.build("name-.").tagged("foo-.", "bar-."));

        assertIsRegistrable(on);
        assertThat(on.getDomain()).isEqualTo("domain-.");
        assertThat(on.getKeyProperty("type")).isEqualTo("type-.");
        assertThat(on.getKeyProperty("name")).isEqualTo("name-.");
        assertThat(on.getKeyProperty("foo-.")).isEqualTo("bar-.");
    }

    @Test
    public void allowsEmptyNameAndTagValue() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        ObjectName on = f.createName("", "", MetricName.build("").tagged("", ""));

        assertIsRegistrable(on);
        assertThat(on.getDomain()).isEqualTo("");
        assertThat(on.getKeyProperty("type")).isEqualTo("");
        assertThat(on.getKeyProperty("name")).isEqualTo("");
        assertThat(on.getKeyProperty("_")).isEqualTo("");
    }

    @Test
    public void tagsDontOverrideNameAndType() {
        DefaultObjectNameFactory f = new DefaultObjectNameFactory();
        MetricName metric = MetricName.build("name").tagged("type", "t", "domain", "d", "name", "n", "k", "v");
        ObjectName on = f.createName("type", "domain", metric);

        assertIsRegistrable(on);
        assertThat(on.getDomain()).isEqualTo("domain");
        assertThat(on.getKeyProperty("type")).isEqualTo("type");
        assertThat(on.getKeyProperty("name")).isEqualTo("name");
        assertThat(on.getKeyProperty("k")).isEqualTo("v");
    }

    private static void assertIsRegistrable(ObjectName on) {
        assertThatCode(() -> new ObjectName(on.toString()))
                .as("%s should be parseable", on)
                .doesNotThrowAnyException();
        assertThat(on.isPattern())
                .as("%s should not be a pattern", on)
                .isFalse();
    }

    private static Stream<UnsafeName> unsafeNames() {
        // raw, quoted, asKey, asDomain
        return Stream.of(
                new UnsafeName("a,b", "\"a,b\"", "a_b", "a,b"),
                new UnsafeName("a=b", "\"a=b\"", "a_b", "a=b"),
                new UnsafeName("a:b", "\"a:b\"", "a_b", "a_b"),
                new UnsafeName("a\nb", "\"a\\nb\"", "a_b", "a_b"),
                new UnsafeName("a*b", "\"a\\*b\"", "a_b", "a_b"),
                new UnsafeName("a?b", "\"a\\?b\"", "a_b", "a_b"),
                new UnsafeName("quotes(\"ABcd\")", "\"quotes(\\\"ABcd\\\")\"", "quotes(\"ABcd\")","quotes(\"ABcd\")")
        );
    }

    private record UnsafeName(String raw, String quoted, String asKey, String asDomain) {
        @Override
        public String toString() {
            return raw;
        }
    }
}
