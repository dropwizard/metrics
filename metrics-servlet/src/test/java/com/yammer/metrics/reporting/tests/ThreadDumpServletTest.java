package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.ThreadDumpServlet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class ThreadDumpServletTest {
    private final VirtualMachineMetrics vm = mock(VirtualMachineMetrics.class);
    private final ThreadDumpServlet servlet = new ThreadDumpServlet(vm);

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    private final ServletOutputStream output = mock(ServletOutputStream.class);

    @Before
    public void setUp() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        when(response.getOutputStream()).thenReturn(output);
    }

    @Test
    public void printsAThreadDumpOnGET() throws Exception {
        servlet.service(request, response);

        final InOrder inOrder = inOrder(response, output, vm);
        inOrder.verify(response).setStatus(200);
        inOrder.verify(response).setContentType("text/plain");
        inOrder.verify(vm).threadDump(output);
        inOrder.verify(output).close();
    }
}
