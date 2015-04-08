package com.codahale.metrics.httpclient;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.junit.Test;

import static com.codahale.metrics.httpclient.HttpRequestMetricNameStrategies.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HttpRequestMetricNameStrategiesTest {

    @Test
    public void methodOnlyWithName() {
        assertThat(METHOD_ONLY.getNameForActive(HttpClient.class, "some-service", new HttpGet("/whatever")),
                   is("org.apache.http.client.HttpClient.some-service.get.active"));
        assertThat(METHOD_ONLY.getNameForDuration(HttpClient.class, "some-service", new HttpGet("/whatever")),
                is("org.apache.http.client.HttpClient.some-service.get.duration"));
    }

    @Test
    public void methodOnlyWithoutName() {
        assertThat(METHOD_ONLY.getNameForActive(HttpClient.class, null, new HttpGet("/whatever")),
                is("org.apache.http.client.HttpClient.get.active"));
        assertThat(METHOD_ONLY.getNameForDuration(HttpClient.class, null, new HttpGet("/whatever")),
                is("org.apache.http.client.HttpClient.get.duration"));
    }

    @Test
    public void hostAndMethodWithName() {
        assertThat(HOST_AND_METHOD.getNameForActive(HttpClient.class, "some-service", new HttpPost("http://my.host.com/whatever")),
                   is("org.apache.http.client.HttpClient.some-service.my.host.com.post.active"));
        assertThat(HOST_AND_METHOD.getNameForDuration(HttpClient.class, "some-service", new HttpPost("http://my.host.com/whatever")),
                is("org.apache.http.client.HttpClient.some-service.my.host.com.post.duration"));
    }

    @Test
    public void hostAndMethodWithoutName() {
        assertThat(HOST_AND_METHOD.getNameForActive(HttpClient.class, null, new HttpPost("http://my.host.com/whatever")),
                is("org.apache.http.client.HttpClient.my.host.com.post.active"));
        assertThat(HOST_AND_METHOD.getNameForDuration(HttpClient.class, null, new HttpPost("http://my.host.com/whatever")),
                is("org.apache.http.client.HttpClient.my.host.com.post.duration"));
    }

    @Test
    public void querylessUrlAndMethodWithName() {
        assertThat(QUERYLESS_URL_AND_METHOD.getNameForActive(HttpClient.class,
                        "some-service",
                        new HttpPut("https://thing.com:8090/my/path?ignore=this&and=this")),
                is("org.apache.http.client.HttpClient.some-service.https://thing.com:8090/my/path.put.active"));
        assertThat(QUERYLESS_URL_AND_METHOD.getNameForDuration(HttpClient.class,
                        "some-service",
                        new HttpPut("https://thing.com:8090/my/path?ignore=this&and=this")),
                is("org.apache.http.client.HttpClient.some-service.https://thing.com:8090/my/path.put.duration"));
    }
}
