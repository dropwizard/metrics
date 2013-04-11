package com.yammer.metrics.httpclient;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.junit.Test;

import static com.yammer.metrics.httpclient.HttpClientMetricNameStrategies.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HttpClientMetricNameStrategiesTest {

    @Test
    public void methodOnlyWithName() {
        assertThat(METHOD_ONLY.getNameFor("some-service", new HttpGet("/whatever")),
                   is("org.apache.http.client.HttpClient.some-service.get-requests"));
    }

    @Test
    public void methodOnlyWithoutName() {
        assertThat(METHOD_ONLY.getNameFor(null, new HttpGet("/whatever")),
                is("org.apache.http.client.HttpClient.get-requests"));
    }

    @Test
    public void hostAndMethodWithName() {
        assertThat(HOST_AND_METHOD.getNameFor("some-service", new HttpPost("http://my.host.com/whatever")),
                   is("org.apache.http.client.HttpClient.some-service.my.host.com.post-requests"));
    }

    @Test
    public void hostAndMethodWithoutName() {
        assertThat(HOST_AND_METHOD.getNameFor(null, new HttpPost("http://my.host.com/whatever")),
                is("org.apache.http.client.HttpClient.my.host.com.post-requests"));
    }

    @Test
    public void querylessUrlAndMethodWithName() {
        assertThat(QUERYLESS_URL_AND_METHOD.getNameFor(
                "some-service",
                new HttpPut("https://thing.com:8090/my/path?ignore=this&and=this")),
                is("org.apache.http.client.HttpClient.some-service.https://thing.com:8090/my/path.put-requests"));
    }
}
