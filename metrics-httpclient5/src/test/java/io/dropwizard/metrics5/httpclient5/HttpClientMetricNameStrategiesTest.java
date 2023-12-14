package io.dropwizard.metrics5.httpclient5;

import io.dropwizard.metrics5.MetricName;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.HttpRequestWrapper;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static io.dropwizard.metrics5.httpclient5.HttpClientMetricNameStrategies.HOST_AND_METHOD;
import static io.dropwizard.metrics5.httpclient5.HttpClientMetricNameStrategies.METHOD_ONLY;
import static io.dropwizard.metrics5.httpclient5.HttpClientMetricNameStrategies.QUERYLESS_URL_AND_METHOD;
import static org.assertj.core.api.Assertions.assertThat;

class HttpClientMetricNameStrategiesTest {

    @Test
    void methodOnlyWithName() {
        assertThat(METHOD_ONLY.getNameFor("some-service", new HttpGet("/whatever")))
                .isEqualTo(MetricName.build("org.apache.hc.client5.http.classic.HttpClient.some-service.get-requests"));
    }

    @Test
    void methodOnlyWithoutName() {
        assertThat(METHOD_ONLY.getNameFor(null, new HttpGet("/whatever")))
                .isEqualTo(MetricName.build("org.apache.hc.client5.http.classic.HttpClient.get-requests"));
    }

    @Test
    void hostAndMethodWithName() {
        assertThat(HOST_AND_METHOD.getNameFor("some-service", new HttpPost("http://my.host.com/whatever")))
                .isEqualTo(MetricName.build("org.apache.hc.client5.http.classic.HttpClient.some-service.my.host.com.post-requests"));
    }

    @Test
    void hostAndMethodWithoutName() {
        assertThat(HOST_AND_METHOD.getNameFor(null, new HttpPost("http://my.host.com/whatever")))
                .isEqualTo(MetricName.build("org.apache.hc.client5.http.classic.HttpClient.my.host.com.post-requests"));
    }

    @Test
    void hostAndMethodWithNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPost("http://my.host.com/whatever"));

        assertThat(HOST_AND_METHOD.getNameFor("some-service", request))
                .isEqualTo(MetricName.build("org.apache.hc.client5.http.classic.HttpClient.some-service.my.host.com.post-requests"));
    }

    @Test
    void hostAndMethodWithoutNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPost("http://my.host.com/whatever"));

        assertThat(HOST_AND_METHOD.getNameFor(null, request))
                .isEqualTo(MetricName.build("org.apache.hc.client5.http.classic.HttpClient.my.host.com.post-requests"));
    }

    @Test
    void querylessUrlAndMethodWithName() {
        assertThat(QUERYLESS_URL_AND_METHOD.getNameFor(
                "some-service",
                new HttpPut("https://thing.com:8090/my/path?ignore=this&and=this")))
                .isEqualTo(MetricName.build("org.apache.hc.client5.http.classic.HttpClient.some-service.https://thing.com:8090/my/path.put-requests"));
    }

    @Test
    void querylessUrlAndMethodWithNameInWrappedRequest() throws URISyntaxException {
        HttpRequest request = rewriteRequestURI(new HttpPut("https://thing.com:8090/my/path?ignore=this&and=this"));
        assertThat(QUERYLESS_URL_AND_METHOD.getNameFor("some-service", request))
                .isEqualTo(MetricName.build("org.apache.hc.client5.http.classic.HttpClient.some-service.https://thing.com:8090/my/path.put-requests"));
    }

    private static HttpRequest rewriteRequestURI(HttpRequest request) throws URISyntaxException {
        URI uri = new URIBuilder(request.getUri()).setFragment(null).build();
        HttpRequestWrapper wrapper = new HttpRequestWrapper(request);
        wrapper.setUri(uri);

        return wrapper;
    }
}
