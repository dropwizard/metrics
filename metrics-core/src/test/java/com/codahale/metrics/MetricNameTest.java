package com.codahale.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MetricNameTest {
    @Test
    public void testEmpty() throws Exception {
        assertThat(MetricName.EMPTY.getTags()).isEqualTo(MetricName.EMPTY_TAGS);
        assertThat(MetricName.EMPTY.getKey()).isEqualTo(null);
        assertThat(new MetricName().getTags()).isEqualTo(MetricName.EMPTY_TAGS);

        assertThat(MetricName.EMPTY).isEqualTo(new MetricName());
        assertThat(MetricName.build()).isEqualTo(MetricName.EMPTY);
        assertThat(MetricName.EMPTY.resolve(null)).isEqualTo(MetricName.EMPTY);
    }

    @Test
    public void testEmptyResolve() throws Exception {
        final MetricName name = new MetricName();
        assertThat(name.resolve("foo")).isEqualTo(new MetricName("foo"));
    }

    @Test
    public void testResolveToEmpty() throws Exception {
        final MetricName name = new MetricName("foo");
        assertThat(name.resolve(null)).isEqualTo(new MetricName("foo"));
    }

    @Test
    public void testResolve() throws Exception {
        final MetricName name = new MetricName("foo");
        assertThat(name.resolve("bar")).isEqualTo(new MetricName("foo.bar"));
    }

    @Test
    public void testResolveBothEmpty() throws Exception {
        final MetricName name = new MetricName(null);
        assertThat(name.resolve(null)).isEqualTo(new MetricName());
    }

    @Test
    public void testAddTagsVarious() {
        final Map<String, String> refTags = new HashMap<String, String>();
        refTags.put("foo", "bar");
        final MetricName test = MetricName.EMPTY.tagged("foo", "bar");
        final MetricName test2 = MetricName.EMPTY.tagged(refTags);

        assertThat(test).isEqualTo(new MetricName(null, refTags));
        assertThat(test.getTags()).isEqualTo(refTags);

        assertThat(test2).isEqualTo(new MetricName(null, refTags));
        assertThat(test2.getTags()).isEqualTo(refTags);
    }

    @Test
    public void testTaggedMoreArguments() {
        final Map<String, String> refTags = new HashMap<String, String>();
        refTags.put("foo", "bar");
        refTags.put("baz", "biz");
        assertThat(MetricName.EMPTY.tagged("foo", "bar", "baz", "biz").getTags()).isEqualTo(refTags);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testTaggedNotPairs() {
        MetricName.EMPTY.tagged("foo");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testTaggedNotPairs2() {
        MetricName.EMPTY.tagged("foo", "bar", "baz");
    }

    @Test
    public void testCompareTo() {
        final MetricName a = MetricName.EMPTY.tagged("foo", "bar");
        final MetricName b = MetricName.EMPTY.tagged("foo", "baz");

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
        assertThat(b.compareTo(b)).isEqualTo(0);
        assertThat(b.resolve("key").compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(b.resolve("key"))).isGreaterThan(0);
    }
}
