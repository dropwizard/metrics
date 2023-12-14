package io.dropwizard.metrics5.httpclient;

import io.dropwizard.metrics5.MetricName;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.utils.URIUtils;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static io.dropwizard.metrics5.httpclient.HttpClientMetricNameStrategies.HOST_AND_METHOD;
import static io.dropwizard.metrics5.httpclient.HttpClientMetricNameStrategies.METHOD_ONLY;
import static io.dropwizard.metrics5.httpclient.HttpClientMetricNameStrategies.PATH_AND_METHOD;
import static io.dropwizard.metrics5.httpclient.HttpClientMetricNameStrategies.QUERYLESS_URL_AND_METHOD;
import static org.assertj.core.api.Assertions.assertThat;

class HttpClientMetricNameStrategiesTest {

    @Test
    void methodOnlyWithName() {
        assertThat(METHOD_ONLY.getNameFor("some-service", new HttpGet("/whatever")))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.some-service.get-requests"));
    }

    @Test
    void methodOnlyWithoutName() {
        assertThat(METHOD_ONLY.getNameFor(null, new HttpGet("/whatever")))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.get-requests"));
    }

    @Test
    void hostAndMethodWithName() {
        assertThat(HOST_AND_METHOD.getNameFor("some-service", new HttpPost("http://my.host.com/whatever")))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.some-service.my.host.com.post-requests"));
    }

    @Test
    void hostAndMethodWithoutName() {
        assertThat(HOST_AND_METHOD.getNameFor(null, new HttpPost("http://my.host.com/whatever")))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.my.host.com.post-requests"));
    }

    @Test
    void hostAndMethodWithNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPost("http://my.host.com/whatever"));

        assertThat(HOST_AND_METHOD.getNameFor("some-service", request))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.some-service.my.host.com.post-requests"));
    }

    @Test
    void hostAndMethodWithoutNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPost("http://my.host.com/whatever"));

        assertThat(HOST_AND_METHOD.getNameFor(null, request))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.my.host.com.post-requests"));
    }

    @Test
    void pathAndMethodWithName() {
        assertThat(PATH_AND_METHOD.getNameFor("some-service", new HttpPost("http://my.host.com/whatever/happens")))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.some-service./whatever/happens.post-requests"));
    }

    @Test
    void pathAndMethodWithoutName() {
        assertThat(PATH_AND_METHOD.getNameFor(null, new HttpPost("http://my.host.com/whatever/happens")))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient./whatever/happens.post-requests"));
    }

    @Test
    void pathAndMethodWithNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPost("http://my.host.com/whatever/happens"));
        assertThat(PATH_AND_METHOD.getNameFor("some-service", request))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.some-service./whatever/happens.post-requests"));
    }

    @Test
    void pathAndMethodWithoutNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPost("http://my.host.com/whatever/happens"));
        assertThat(PATH_AND_METHOD.getNameFor(null, request))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient./whatever/happens.post-requests"));
    }

    @Test
    void querylessUrlAndMethodWithName() {
        assertThat(QUERYLESS_URL_AND_METHOD.getNameFor(
                "some-service",
                new HttpPut("https://thing.com:8090/my/path?ignore=this&and=this")))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.some-service.https://thing.com:8090/my/path.put-requests"));
    }

    @Test
    void querylessUrlAndMethodWithNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPut("https://thing.com:8090/my/path?ignore=this&and=this"));
        assertThat(QUERYLESS_URL_AND_METHOD.getNameFor("some-service", request))
                .isEqualTo(MetricName.build("org.apache.http.client.HttpClient.some-service.https://thing.com:8090/my/path.put-requests"));
    }

    private static HttpRequest rewriteRequestURI(HttpRequest request) throws URISyntaxException {
        HttpRequestWrapper wrapper = HttpRequestWrapper.wrap(request);
        URI uri = URIUtils.rewriteURI(wrapper.getURI(), null, true);
        wrapper.setURI(uri);

        return wrapper;
    }
}
