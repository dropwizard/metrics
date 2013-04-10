package com.yammer.metrics.httpclient.tests;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.httpclient.strategies.HostAndMethodMetricNameStrategy;
import org.apache.http.client.methods.HttpHead;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HostAndMethodMetricNameStrategyTest {

    @Test
    public void head() {
        MetricName metricName = new HostAndMethodMetricNameStrategy()
                .getNameFor(new HttpHead("https://some.domain.com"));

        assertThat(metricName.getDomain(), is("default"));
        assertThat(metricName.getType(), is("some.domain.com"));
        assertThat(metricName.getName(), is("HEAD"));
    }

    @Test
    public void overriddenDomain() {
        MetricName metricName = new HostAndMethodMetricNameStrategy("something-else")
                .getNameFor(new HttpHead("http://whatever"));

        assertThat(metricName.getDomain(), is("something-else"));
    }
}
