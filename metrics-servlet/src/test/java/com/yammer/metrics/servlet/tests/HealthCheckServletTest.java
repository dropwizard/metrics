package com.yammer.metrics.servlet.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.servlet.HealthCheckServlet;

@RunWith(Parameterized.class)
public class HealthCheckServletTest {
    private final HealthCheckRegistry registry = mock(HealthCheckRegistry.class);
    private final HealthCheckServlet servlet = new HealthCheckServlet(registry);
    private final SortedMap<String, HealthCheck.Result> results = new TreeMap<String, HealthCheck.Result>();

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private final String contentType;

    public HealthCheckServletTest(String contentType) {
        super();
        this.contentType = contentType;
    }

    @Parameterized.Parameters
    public static Collection contentTypes() {
        return Arrays.asList(new Object[][] {
                {HealthCheckServlet.DEFAULT_CONTENT_TYPE},
                {HealthCheckServlet.JSON_CONTENT_TYPE}});
    }

    @Before
    public void setUp() throws Exception {
        when(request.getHeader("Accept")).thenReturn(this.contentType);
        when(request.getMethod()).thenReturn("GET");

        when(registry.runHealthChecks()).thenReturn(results);

        when(response.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(output)));
    }

    @Test
    public void returnsNotImplementedIfNoHealthChecksAreRegistered() throws Exception {
        results.clear();

        servlet.service(request, response);

        String expectedOutput = "! No health checks registered.\n";
        if (contentType.equals(HealthCheckServlet.JSON_CONTENT_TYPE)) {
            expectedOutput = "";
        }
        assertThat(output.toString().replaceAll("\r\n", "\n"),
                is(expectedOutput));

        verify(response).setStatus(501);
        verify(response).setContentType(contentType);
    }

    @Test
    public void returnsOkIfAllHealthChecksAreHealthy() throws Exception {
        results.put("one", HealthCheck.Result.healthy());
        results.put("two", HealthCheck.Result.healthy("msg"));

        servlet.service(request, response);

        String expectedOutput = "* one: OK\n* two: OK\n  msg\n";
        if (contentType.equals(HealthCheckServlet.JSON_CONTENT_TYPE)) {
            expectedOutput = "[{\"name\":\"one\",\"healthy\":true,\"message\":null},"
                    +"{\"name\":\"two\",\"healthy\":true,\"message\":\"msg\"}]";
        }
        assertThat(output.toString(),
                is(expectedOutput));

        verify(response).setStatus(200);
        verify(response).setContentType(contentType);
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void returnsServerErrorIfHealthChecksAreUnhealthy() throws Exception {
        final IOException ex = mock(IOException.class);
        when(ex.getMessage()).thenReturn("ex msg");
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((PrintWriter) invocation.getArguments()[0]).println("stack trace");
                return null;
            }
        }).when(ex).printStackTrace(any(PrintWriter.class));

        results.put("one", HealthCheck.Result.unhealthy("msg"));
        results.put("two", HealthCheck.Result.unhealthy(ex));

        servlet.service(request, response);

        String expectedOutput = "! one: ERROR\n" +
                "!  msg\n" +
                "! two: ERROR\n" +
                "!  ex msg\n" +
                "\n" +
                "stack trace\n\n";
        if (contentType.equals(HealthCheckServlet.JSON_CONTENT_TYPE)) {
            expectedOutput = "[{\"name\":\"one\",\"healthy\":false,\"message\":\"msg\"},"
                    + "{\"name\":\"two\",\"healthy\":false,\"message\":\"ex msg\","
                    + "\"error\":\"stack trace\\n\"}]";
        }

        assertThat(output.toString().replaceAll("\r\n", "\n"),
                is(expectedOutput));
        verify(response).setStatus(500);
        verify(response).setContentType(contentType);
    }

    @Test
    public void picksUpTheHealthCheckRegistryFromTheConfig() throws Exception {
        final SortedMap<String, HealthCheck.Result> otherResults = new TreeMap<String, HealthCheck.Result>();
        otherResults.put("one", HealthCheck.Result.healthy());

        final HealthCheckRegistry reg = mock(HealthCheckRegistry.class);
        when(reg.runHealthChecks()).thenReturn(otherResults);

        final ServletContext context = mock(ServletContext.class);
        when(context.getAttribute(HealthCheckServlet.REGISTRY_ATTRIBUTE)).thenReturn(reg);

        final ServletConfig config = mock(ServletConfig.class);
        when(config.getServletContext()).thenReturn(context);

        servlet.init(config);
        servlet.service(request, response);

        String expectedOutput = "* one: OK\n";
        if (contentType.equals(HealthCheckServlet.JSON_CONTENT_TYPE)) {
            expectedOutput = "[{\"name\":\"one\",\"healthy\":true,\"message\":null}]";
        }
        assertThat(output.toString(),is(expectedOutput));

        verify(response).setStatus(200);
        verify(response).setContentType(contentType);
    }

    @Test
    public void doesNotThrowAnErrorIfTheConfigIsWeird() throws Exception {
        final ServletContext context = mock(ServletContext.class);
        when(context.getAttribute(HealthCheckServlet.REGISTRY_ATTRIBUTE)).thenReturn("yes wait what");

        final ServletConfig config = mock(ServletConfig.class);
        when(config.getServletContext()).thenReturn(context);

        servlet.init(config);
        servlet.service(request, response);
    }
}
