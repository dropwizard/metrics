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
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedHandlerTest {
    private final HttpClient client = new HttpClient();
    private final MetricRegistry registry = new MetricRegistry();
    private final Server server = new Server();
    private final ServerConnector connector = new ServerConnector(server);
    private final InstrumentedHandler handler = new InstrumentedHandler(registry);

    @Before
    public void setUp() throws Exception {
        handler.setName("handler");
        handler.setHandler(new TestHandler());
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
    public void hasAName() throws Exception {
        assertThat(handler.getName())
            .isEqualTo("handler");
    }

    @Test
    public void createsMetricsForTheHandler() throws Exception {
        final ContentResponse response = client.GET(uri("/hello"));

        assertThat(response.getStatus())
            .isEqualTo(404);

        assertThat(registry.getNames())
            .containsOnly(
                MetricRegistry.name(TestHandler.class, "handler.1xx-responses"),
                MetricRegistry.name(TestHandler.class, "handler.2xx-responses"),
                MetricRegistry.name(TestHandler.class, "handler.3xx-responses"),
                MetricRegistry.name(TestHandler.class, "handler.4xx-responses"),
                MetricRegistry.name(TestHandler.class, "handler.5xx-responses"),
                MetricRegistry.name(TestHandler.class, "handler.percent-4xx-1m"),
                MetricRegistry.name(TestHandler.class, "handler.percent-4xx-5m"),
                MetricRegistry.name(TestHandler.class, "handler.percent-4xx-15m"),
                MetricRegistry.name(TestHandler.class, "handler.percent-5xx-1m"),
                MetricRegistry.name(TestHandler.class, "handler.percent-5xx-5m"),
                MetricRegistry.name(TestHandler.class, "handler.percent-5xx-15m"),
                MetricRegistry.name(TestHandler.class, "handler.requests"),
                MetricRegistry.name(TestHandler.class, "handler.active-suspended"),
                MetricRegistry.name(TestHandler.class, "handler.async-dispatches"),
                MetricRegistry.name(TestHandler.class, "handler.async-timeouts"),
                MetricRegistry.name(TestHandler.class, "handler.get-requests"),
                MetricRegistry.name(TestHandler.class, "handler.put-requests"),
                MetricRegistry.name(TestHandler.class, "handler.active-dispatches"),
                MetricRegistry.name(TestHandler.class, "handler.trace-requests"),
                MetricRegistry.name(TestHandler.class, "handler.other-requests"),
                MetricRegistry.name(TestHandler.class, "handler.connect-requests"),
                MetricRegistry.name(TestHandler.class, "handler.dispatches"),
                MetricRegistry.name(TestHandler.class, "handler.head-requests"),
                MetricRegistry.name(TestHandler.class, "handler.post-requests"),
                MetricRegistry.name(TestHandler.class, "handler.options-requests"),
                MetricRegistry.name(TestHandler.class, "handler.active-requests"),
                MetricRegistry.name(TestHandler.class, "handler.delete-requests"),
                MetricRegistry.name(TestHandler.class, "handler.move-requests")
            );
    }


    @Test
    public void responseTimesAreRecordedForBlockingResponses() throws Exception {

        final ContentResponse response = client.GET(uri("/blocking"));

        assertThat(response.getStatus())
            .isEqualTo(200);

        assertResponseTimesValid();
    }

    @Test
    @Ignore("flaky on virtual machines")
    public void responseTimesAreRecordedForAsyncResponses() throws Exception {

        final ContentResponse response = client.GET(uri("/async"));

        assertThat(response.getStatus())
            .isEqualTo(200);

        assertResponseTimesValid();
    }

    private void assertResponseTimesValid() {
        assertThat(registry.getMeters().get(metricName() + ".2xx-responses")
            .getCount()).isGreaterThan(0L);


        assertThat(registry.getTimers().get(metricName() + ".get-requests")
            .getSnapshot().getMedian()).isGreaterThan(0.0).isLessThan(TimeUnit.SECONDS.toNanos(1));

        assertThat(registry.getTimers().get(metricName() + ".requests")
            .getSnapshot().getMedian()).isGreaterThan(0.0).isLessThan(TimeUnit.SECONDS.toNanos(1));
    }

    private String uri(String path) {
        return "http://localhost:" + connector.getLocalPort() + path;
    }

    private String metricName() {
        return MetricRegistry.name(TestHandler.class.getName(), "handler");
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
            final HttpServletResponse httpServletResponse
        ) throws IOException, ServletException {
            switch (path) {
                case "/blocking":
                    request.setHandled(true);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    httpServletResponse.setStatus(200);
                    httpServletResponse.setContentType("text/plain");
                    httpServletResponse.getWriter().write("some content from the blocking request\n");
                    break;
                case "/async":
                    request.setHandled(true);
                    final AsyncContext context = request.startAsync();
                    Thread t = new Thread(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        httpServletResponse.setStatus(200);
                        httpServletResponse.setContentType("text/plain");
                        final ServletOutputStream servletOutputStream;
                        try {
                            servletOutputStream = httpServletResponse.getOutputStream();
                            servletOutputStream.setWriteListener(
                                new WriteListener() {
                                    @Override
                                    public void onWritePossible() throws IOException {
                                        servletOutputStream.write("some content from the async\n"
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
