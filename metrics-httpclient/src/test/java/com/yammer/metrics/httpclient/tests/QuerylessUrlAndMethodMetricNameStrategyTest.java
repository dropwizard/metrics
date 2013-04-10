package com.yammer.metrics.httpclient.tests;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.httpclient.strategies.QuerylessUrlAndMethodMetricNameStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class QuerylessUrlAndMethodMetricNameStrategyTest {

    @Test
    public void get() {
        MetricName metricName =
                new QuerylessUrlAndMethodMetricNameStrategy()
                .getNameFor(new HttpGet("http://mydomain.com:8090/my/path?ignore=this&and=this"));

        assertThat(metricName.getDomain(), is("default"));
        assertThat(metricName.getName(), is("GET"));
        assertThat(metricName.getType(), is("http://mydomain.com:8090/my/path"));
    }

    @Test
    public void put() {
        MetricName metricName =
                new QuerylessUrlAndMethodMetricNameStrategy()
                        .getNameFor(new HttpPut("http://mydomain.com:8090/my/path?ignore=this&and=this"));

        assertThat(metricName.getDomain(), is("default"));
        assertThat(metricName.getName(), is("PUT"));
        assertThat(metricName.getType(), is("http://mydomain.com:8090/my/path"));
    }

    @Test
    public void doesntBlowUpWhenNoQueryPresent() {
        MetricName metricName =
                new QuerylessUrlAndMethodMetricNameStrategy()
                        .getNameFor(new HttpGet("http://mydomain.com:8090/my/path"));

        assertThat(metricName.getDomain(), is("default"));
        assertThat(metricName.getName(), is("GET"));
        assertThat(metricName.getType(), is("http://mydomain.com:8090/my/path"));
    }

    @Test
    public void overriddenDomain() {
        MetricName metricName =
                new QuerylessUrlAndMethodMetricNameStrategy("my-domain")
                        .getNameFor(new HttpGet("http://whatever/yah"));

        assertThat(metricName.getDomain(), is("my-domain"));
    }


}
