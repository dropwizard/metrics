package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.reporting.PingServlet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PingServletTest {
    private final PingServlet servlet = new PingServlet();

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    private final PrintWriter output = mock(PrintWriter.class);

    @Before
    public void setUp() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        when(response.getWriter()).thenReturn(output);
    }

    @Test
    public void printsPongOnGET() throws Exception {
        servlet.service(request, response);

        final InOrder inOrder = inOrder(response, output);
        inOrder.verify(response).setStatus(200);
        inOrder.verify(response).setContentType("text/plain");
        inOrder.verify(output).println("pong");
        inOrder.verify(output).close();
    }
}
