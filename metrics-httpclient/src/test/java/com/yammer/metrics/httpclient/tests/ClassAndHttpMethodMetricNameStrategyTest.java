package com.yammer.metrics.httpclient.tests;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.httpclient.strategies.ClassAndHttpMethodMetricNameStrategy;
import org.apache.http.client.methods.HttpDelete;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ClassAndHttpMethodMetricNameStrategyTest {

    @Test
    public void delete() {
        MetricName metricName = new ClassAndHttpMethodMetricNameStrategy().getNameFor(new HttpDelete("/whatever"));

        assertThat(metricName.getDomain(), is("org.apache.http.client"));
        assertThat(metricName.getType(), is("HttpClient"));
        assertThat(metricName.getName(), is("delete-requests"));
    }
}
