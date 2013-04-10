package com.yammer.metrics.httpclient.tests;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.httpclient.strategies.MethodOnlyMetricNameStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MethodOnlyMetricNameStrategyTest {

    @Test
    public void get() {
        MetricName name = new MethodOnlyMetricNameStrategy().getNameFor(new HttpGet("/anything"));
        assertThat(name.getName(), is("GET"));
        assertThat(name.getType(), is("http-client"));
        assertThat(name.getDomain(), is("default"));
    }

    @Test
    public void post() {
        MetricName name = new MethodOnlyMetricNameStrategy().getNameFor(new HttpPost("/anything"));
        assertThat(name.getName(), is("POST"));
    }

    @Test
    public void overriddenDomain() {
        MetricName name = new MethodOnlyMetricNameStrategy("my-http-client").getNameFor(new HttpPut("/anything"));
        assertThat(name.getDomain(), is("my-http-client"));
    }
}
