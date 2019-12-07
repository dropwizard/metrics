package com.codahale.metrics.jetty9;

import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedHttpChannelListenerTest {
    private final HttpClient client = new HttpClient();
    private final Server server = new Server();
    private final ServerConnector connector = new ServerConnector(server);
    private final TestHandler handler = new TestHandler();
    private MetricRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = new MetricRegistry();
        connector.addBean(new InstrumentedHttpChannelListener(registry, MetricRegistry.name(TestHandler.class, "handler")));
        server.addConnector(connector);
        server.setHandler(handler);
        server.start();
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        client.stop();
    }

    @Test
    public void createsMetricsForTheHandler() throws Exception {
        final ContentResponse response = client.GET(uri("/hello"));

        assertThat(response.getStatus())
            .isEqualTo(404);

        assertThat(registry.getNames())
            .containsOnly(
                metricName("1xx-responses"),
                metricName("2xx-responses"),
                metricName("3xx-responses"),
                metricName("4xx-responses"),
                metricName("5xx-responses"),
                metricName("percent-4xx-1m"),
                metricName("percent-4xx-5m"),
                metricName("percent-4xx-15m"),
                metricName("percent-5xx-1m"),
                metricName("percent-5xx-5m"),
                metricName("percent-5xx-15m"),
                metricName("requests"),
                metricName("active-suspended"),
                metricName("async-dispatches"),
                metricName("async-timeouts"),
                metricName("get-requests"),
                metricName("put-requests"),
                metricName("active-dispatches"),
                metricName("trace-requests"),
                metricName("other-requests"),
                metricName("connect-requests"),
                metricName("dispatches"),
                metricName("head-requests"),
                metricName("post-requests"),
                metricName("options-requests"),
                metricName("active-requests"),
                metricName("delete-requests"),
                metricName("move-requests")
            );
    }


    @Test
    public void responseTimesAreRecordedForBlockingResponses() throws Exception {

        final ContentResponse response = client.GET(uri("/blocking"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMediaType()).isEqualTo("text/plain");
        assertThat(response.getContentAsString()).isEqualTo("some content from the blocking request");

        assertResponseTimesValid();
    }

    @Test
    public void responseTimesAreRecordedForAsyncResponses() throws Exception {

        final ContentResponse response = client.GET(uri("/async"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMediaType()).isEqualTo("text/plain");
        assertThat(response.getContentAsString()).isEqualTo("some content from the async");

        assertResponseTimesValid();
    }

    private void assertResponseTimesValid() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(registry.getMeters().get(metricName("2xx-responses"))
            .getCount()).isPositive();

        assertThat(registry.getTimers().get(metricName("get-requests"))
            .getSnapshot().getMedian()).isPositive();

        assertThat(registry.getTimers().get(metricName("requests"))
            .getSnapshot().getMedian()).isPositive();
    }

    private String uri(String path) {
        return "http://localhost:" + connector.getLocalPort() + path;
    }

    private String metricName(String metricName) {
        return MetricRegistry.name(TestHandler.class.getName(), "handler", metricName);
    }

    /**
     * test handler.
     * <p>
     * Supports
     * <p>
     * /blocking - uses the standard servlet api
     * /async - uses the 3.1 async api to complete the request
     * <p>
     * all other requests will return 404
     */
    private static class TestHandler extends AbstractHandler {
        @Override
        public void handle(
            String path,
            Request request,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse) throws IOException {
            switch (path) {
                case "/blocking":
                    request.setHandled(true);
                    httpServletResponse.setStatus(200);
                    httpServletResponse.setContentType("text/plain");
                    httpServletResponse.getWriter().write("some content from the blocking request");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        httpServletResponse.setStatus(500);
                        Thread.currentThread().interrupt();
                    }
                    break;
                case "/async":
                    request.setHandled(true);
                    final AsyncContext context = request.startAsync();
                    Thread t = new Thread(() -> {
                        httpServletResponse.setStatus(200);
                        httpServletResponse.setContentType("text/plain");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            httpServletResponse.setStatus(500);
                            Thread.currentThread().interrupt();
                        }
                        final ServletOutputStream servletOutputStream;
                        try {
                            servletOutputStream = httpServletResponse.getOutputStream();
                            servletOutputStream.setWriteListener(
                                new WriteListener() {
                                    @Override
                                    public void onWritePossible() throws IOException {
                                        servletOutputStream.write("some content from the async"
                                            .getBytes(StandardCharsets.UTF_8));
                                        context.complete();
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        context.complete();
                                    }
                                }
                            );
                        } catch (IOException e) {
                            context.complete();
                        }
                    });
                    t.start();
                    break;
                 default:
                     break;
            }
        }
    }
}
