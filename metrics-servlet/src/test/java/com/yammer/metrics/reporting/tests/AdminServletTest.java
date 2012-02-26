package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.reporting.*;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminServletTest {
    private final MetricsServlet metricsServlet = mock(MetricsServlet.class);
    private final HealthCheckServlet healthCheckServlet = mock(HealthCheckServlet.class);
    private final ThreadDumpServlet threadDumpServlet = mock(ThreadDumpServlet.class);
    private final PingServlet pingServlet = mock(PingServlet.class);

    private final ServletConfig config = mock(ServletConfig.class);
    private final ServletContext context = mock(ServletContext.class);

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private final AdminServlet servlet = new AdminServlet(healthCheckServlet,metricsServlet,
                                                           pingServlet, threadDumpServlet,
                                                          "/healthcheck",
                                                          "/metrics",
                                                          "/ping",
                                                          "/threads");

    @Before
    public void setUp() throws Exception {
        when(context.getContextPath()).thenReturn("/context");

        when(config.getServletContext()).thenReturn(context);

        when(request.getMethod()).thenReturn("GET");
        when(request.getServletPath()).thenReturn("/admin");
        when(response.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(output)));

        servlet.init(config);
    }

    @Test
    public void initializesUnderlyingServlets() throws Exception {
        verify(healthCheckServlet).init(config);
        verify(metricsServlet).init(config);
        verify(pingServlet).init(config);
        verify(threadDumpServlet).init(config);
    }

    @Test
    public void rendersAnHTMLPageOnRoot() throws Exception {
        when(request.getPathInfo()).thenReturn("/");

        servlet.service(request, response);

        verify(response).setStatus(200);
        verify(response).setContentType("text/html");

        assertThat(output.toString().replaceAll("\r\n", "\n"),
                   is("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n        " +
                              "\"http://www.w3.org/TR/html4/loose.dtd\">\n<html>\n<head>\n  " +
                              "<title>Metrics</title>\n</head>\n<body>\n  " +
                              "<h1>Operational Menu</h1>\n  <ul>\n    " +
                              "<li><a href=\"/context/admin/metrics?pretty=true\">Metrics</a></li>\n    " +
                              "<li><a href=\"/context/admin/ping\">Ping</a></li>\n    " +
                              "<li><a href=\"/context/admin/threads\">Threads</a></li>\n    " +
                              "<li><a href=\"/context/admin/healthcheck\">Healthcheck</a></li>\n  " +
                              "</ul>\n</body>\n</html>\n"));
    }

    @Test
    public void rendersAnHTMLPageOnMissingURI() throws Exception {
        when(request.getPathInfo()).thenReturn(null);

        servlet.service(request, response);

        verify(response).setStatus(200);
        verify(response).setContentType("text/html");

        assertThat(output.toString().replaceAll("\r\n", "\n"),
                   is("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n        " +
                              "\"http://www.w3.org/TR/html4/loose.dtd\">\n<html>\n<head>\n  " +
                              "<title>Metrics</title>\n</head>\n<body>\n  " +
                              "<h1>Operational Menu</h1>\n  <ul>\n    " +
                              "<li><a href=\"/context/admin/metrics?pretty=true\">Metrics</a></li>\n    " +
                              "<li><a href=\"/context/admin/ping\">Ping</a></li>\n    " +
                              "<li><a href=\"/context/admin/threads\">Threads</a></li>\n    " +
                              "<li><a href=\"/context/admin/healthcheck\">Healthcheck</a></li>\n  " +
                              "</ul>\n</body>\n</html>\n"));
    }

    @Test
    public void forwardsToMetrics() throws Exception {
        when(request.getPathInfo()).thenReturn("/metrics");

        servlet.service(request, response);

        verify(metricsServlet).service(request, response);
    }

    @Test
    public void forwardsToHealthCheck() throws Exception {
        when(request.getPathInfo()).thenReturn("/healthcheck");

        servlet.service(request, response);

        verify(healthCheckServlet).service(request, response);
    }

    @Test
    public void forwardsToPing() throws Exception {
        when(request.getPathInfo()).thenReturn("/ping");

        servlet.service(request, response);

        verify(pingServlet).service(request, response);
    }

    @Test
    public void forwardsToThreadDump() throws Exception {
        when(request.getPathInfo()).thenReturn("/threads");

        servlet.service(request, response);

        verify(threadDumpServlet).service(request, response);
    }

    @Test
    public void everythingElseIsNotFound() throws Exception {
        when(request.getPathInfo()).thenReturn("/wobble");

        servlet.service(request, response);

        verify(response).sendError(404);
    }
}
