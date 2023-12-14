package com.codahale.metrics.httpclient5;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.HttpRequestWrapper;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static com.codahale.metrics.httpclient5.HttpClientMetricNameStrategies.HOST_AND_METHOD;
import static com.codahale.metrics.httpclient5.HttpClientMetricNameStrategies.METHOD_ONLY;
import static com.codahale.metrics.httpclient5.HttpClientMetricNameStrategies.QUERYLESS_URL_AND_METHOD;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpClientMetricNameStrategiesTest {

    @Test
    public void methodOnlyWithName() {
        assertThat(METHOD_ONLY.getNameFor("some-service", new HttpGet("/whatever")))
                .isEqualTo("org.apache.hc.client5.http.classic.HttpClient.some-service.get-requests");
    }

    @Test
    public void methodOnlyWithoutName() {
        assertThat(METHOD_ONLY.getNameFor(null, new HttpGet("/whatever")))
                .isEqualTo("org.apache.hc.client5.http.classic.HttpClient.get-requests");
    }

    @Test
    public void hostAndMethodWithName() {
        assertThat(HOST_AND_METHOD.getNameFor("some-service", new HttpPost("http://my.host.com/whatever")))
                .isEqualTo("org.apache.hc.client5.http.classic.HttpClient.some-service.my.host.com.post-requests");
    }

    @Test
    public void hostAndMethodWithoutName() {
        assertThat(HOST_AND_METHOD.getNameFor(null, new HttpPost("http://my.host.com/whatever")))
                .isEqualTo("org.apache.hc.client5.http.classic.HttpClient.my.host.com.post-requests");
    }

    @Test
    public void hostAndMethodWithNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPost("http://my.host.com/whatever"));

        assertThat(HOST_AND_METHOD.getNameFor("some-service", request))
                .isEqualTo("org.apache.hc.client5.http.classic.HttpClient.some-service.my.host.com.post-requests");
    }

    @Test
    public void hostAndMethodWithoutNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPost("http://my.host.com/whatever"));

        assertThat(HOST_AND_METHOD.getNameFor(null, request))
                .isEqualTo("org.apache.hc.client5.http.classic.HttpClient.my.host.com.post-requests");
    }

    @Test
    public void querylessUrlAndMethodWithName() {
        assertThat(QUERYLESS_URL_AND_METHOD.getNameFor(
                "some-service", new HttpPut("https://thing.com:8090/my/path?ignore=this&and=this")))
                .isEqualTo("org.apache.hc.client5.http.classic.HttpClient.some-service.https://thing.com:8090/my/path.put-requests");
    }

    @Test
    public void querylessUrlAndMethodWithNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPut("https://thing.com:8090/my/path?ignore=this&and=this"));
        assertThat(QUERYLESS_URL_AND_METHOD.getNameFor("some-service", request))
                .isEqualTo("org.apache.hc.client5.http.classic.HttpClient.some-service.https://thing.com:8090/my/path.put-requests");
    }

    private static HttpRequest rewriteRequestURI(HttpRequest request) throws URISyntaxException {
        URI uri = new URIBuilder(request.getUri()).setFragment(null).build();
        HttpRequestWrapper wrapper = new HttpRequestWrapper(request);
        wrapper.setUri(uri);

        return wrapper;
    }
}
