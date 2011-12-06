package com.yammer.metrics.core.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import javax.management.ObjectName;

import org.junit.Test;

import com.yammer.metrics.core.MetricName;

public class MetricNameTest {
    private final MetricName name = new MetricName("group", "type", "name", "scope");

    @Test
    public void hasAGroup() throws Exception {
        assertThat(name.getGroup(),
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
    public void hasAnMBeanName() throws Exception {
        assertThat(name.getMBeanName(),
                   equalTo(new ObjectName("group:type=type,scope=scope,name=name")));
                   //eq("bean"));
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(name.toString(),
                   is("group:type=type,scope=scope,name=name"));
    }

    @Test
    public void createsNamesForSimpleMetrics() throws Exception {
        final MetricName simple = new MetricName(MetricNameTest.class, "name");
        
        assertThat("it uses the package name as the group",
                   simple.getGroup(),
                   is(MetricNameTest.class.getPackage().getName()));

        assertThat("it uses the class name as the type",
                   simple.getType(),
                   is(MetricNameTest.class.getSimpleName()));

        assertThat("it doesn't have a scope",
                   simple.hasScope(),
                   is(false));

        assertThat("it has a name",
                   simple.getName(),
                   is("name"));

        final String mbeanName = String.format(
                "%s:type=%s,name=name",
                MetricNameTest.class.getPackage().getName(),
                MetricNameTest.class.getSimpleName());
        assertThat("it has an MBean name",
                   simple.getMBeanName(),
                   equalTo(new ObjectName(mbeanName)));
    }

    @Test
    public void createsNamesForScopedMetrics() throws Exception {
        final MetricName scoped = new MetricName(MetricNameTest.class, "name", "scope");

        assertThat("it uses the package name as the group",
                   scoped.getGroup(),
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

        final String mbeanName = String.format(
                "%s:type=%s,scope=scope,name=name",
                MetricNameTest.class.getPackage().getName(),
                MetricNameTest.class.getSimpleName());
        assertThat("it has an MBean name",
                   scoped.getMBeanName(),
                   equalTo(new ObjectName(mbeanName)));
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
        assertThat(new MetricName("group", "type", "name", "scope", new ObjectName("group:type=type,scope=scope,name=name")).hashCode(),
                   is(equalTo(new MetricName("group", "type", "name", "scope", new ObjectName("group:type=type,scope=scope,name=name")).hashCode())));
        
        assertThat(new MetricName("group", "type", "name", "scope", new ObjectName("group:type=type,scope=scope,name=name")).hashCode(),
                   is(not(equalTo(new MetricName("group", "type", "name", "scope", new ObjectName("group:type=type,scope=scope,name=another-name")).hashCode()))));
    }
}
