package io.dropwizard.metrics5;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricNameTest {
    @Test
    public void testEmpty() {
        assertThat(MetricName.EMPTY.getTags()).isEmpty();
        assertThat(MetricName.EMPTY.getKey()).isEqualTo("");

        assertThat(MetricName.build()).isEqualTo(MetricName.EMPTY);
        assertThat(MetricName.EMPTY.resolve((String)null)).isEqualTo(MetricName.EMPTY);
    }

    @Test
    public void testEmptyResolve() {
        final MetricName name = MetricName.build();
        assertThat(name.resolve("foo")).isEqualTo(MetricName.build("foo"));
    }

    @Test
    public void testResolveToEmpty() {
        final MetricName name = MetricName.build("foo");
        assertThat(name.resolve((String)null)).isEqualTo(MetricName.build("foo"));
    }

    @Test
    public void testResolve() {
        final MetricName name = MetricName.build("foo");
        assertThat(name.resolve("bar")).isEqualTo(MetricName.build("foo.bar"));
    }

    @Test
    public void testResolveBothEmpty() {
        final MetricName name = MetricName.build();
        assertThat(name.resolve((String)null)).isEqualTo(MetricName.EMPTY);
    }

    @Test
    public void testAddTagsVarious() {
        final Map<String, String> refTags = new HashMap<String, String>();
        refTags.put("foo", "bar");
        final MetricName test = MetricName.EMPTY.tagged("foo", "bar");
        final MetricName test2 = MetricName.EMPTY.tagged(refTags);

        assertThat(test).isEqualTo(new MetricName("", refTags));
        assertThat(test.getTags()).isEqualTo(refTags);

        assertThat(test2).isEqualTo(new MetricName("", refTags));
        assertThat(test2.getTags()).isEqualTo(refTags);
    }

    @Test
    public void testTaggedMoreArguments() {
        final Map<String, String> refTags = new HashMap<String, String>();
        refTags.put("foo", "bar");
        refTags.put("baz", "biz");
        assertThat(MetricName.EMPTY.tagged("foo", "bar", "baz", "biz").getTags()).isEqualTo(refTags);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTaggedNotPairs() {
        MetricName.EMPTY.tagged("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTaggedNotPairs2() {
        MetricName.EMPTY.tagged("foo", "bar", "baz");
    }

    @Test
    public void testCompareTo() {
        final MetricName a = MetricName.EMPTY.tagged("foo", "bar");
        final MetricName b = MetricName.EMPTY.tagged("foo", "baz");

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
        assertThat(b.resolve("key").compareTo(b)).isGreaterThan(0);
        assertThat(b.compareTo(b.resolve("key"))).isLessThan(0);
    }

    @Test
    public void testCompareTo2() {
        final MetricName a = MetricName.EMPTY.tagged("a", "x");
        final MetricName b = MetricName.EMPTY.tagged("b", "x");

        assertThat(MetricName.EMPTY.compareTo(a)).isLessThan(0);
        assertThat(MetricName.EMPTY.compareTo(b)).isLessThan(0);
        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
    }
}
