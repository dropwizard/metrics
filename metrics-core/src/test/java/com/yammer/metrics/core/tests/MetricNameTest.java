package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.MetricName;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MetricNameTest {
    private final MetricName name = new MetricName("group", "type", "name", "scope");

    @Test
    public void hasAGroup() throws Exception {
        assertThat(name.getDomain(),
                   is("group"));
    }

    @Test
    public void hasAType() throws Exception {
        assertThat(name.getType(),
                   is("type"));
    }

    @Test
    public void hasAName() throws Exception {
        assertThat(name.getName(),
                   is("name"));
    }

    @Test
    public void hasAScope() throws Exception {
        assertThat(name.getScope(),
                   is("scope"));
        
        assertThat(name.hasScope(),
                   is(true));
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(name.toString(),
                   is("group.type.name.scope"));
    }

    @Test
    public void createsNamesForSimpleMetrics() throws Exception {
        final MetricName simple = new MetricName(MetricNameTest.class, "name");
        
        assertThat("it uses the package name as the group",
                   simple.getDomain(),
                   is("com.yammer.metrics.core.tests"));

        assertThat("it uses the class name as the type",
                   simple.getType(),
                   is("MetricNameTest"));

        assertThat("it doesn't have a scope",
                   simple.hasScope(),
                   is(false));

        assertThat("it has a name",
                   simple.getName(),
                   is("name"));
    }

    @Test
    public void createsNamesForScopedMetrics() throws Exception {
        final MetricName scoped = new MetricName(MetricNameTest.class, "name", "scope");

        assertThat("it uses the package name as the group",
                   scoped.getDomain(),
                   is("com.yammer.metrics.core.tests"));

        assertThat("it uses the class name as the type",
                   scoped.getType(),
                   is("MetricNameTest"));

        assertThat("it has a scope",
                   scoped.getScope(),
                   is("scope"));

        assertThat("it has a name",
                   scoped.getName(),
                   is("name"));
    }

    @Test
    public void hasAWorkingEqualsImplementation() throws Exception {
        assertThat(name,
                   is(equalTo(name)));

        assertThat(name,
                   is(not(equalTo(null))));

        assertThat(name,
                   is(not(equalTo((Object) ""))));
        
        assertThat(name,
                   is(equalTo(new MetricName("group", "type", "name", "scope"))));
    }

    @Test
    public void hasAWorkingHashCodeImplementation() throws Exception {
        assertThat(new MetricName("group", "type", "name", "scope").hashCode(),
                   is(equalTo(new MetricName("group", "type", "name", "scope").hashCode())));
        
        assertThat(new MetricName("group", "type", "name", "scope").hashCode(),
                   is(not(equalTo(new MetricName("group", "type", "name", "scope2").hashCode()))));
    }
}
