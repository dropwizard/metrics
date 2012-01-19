package com.yammer.metrics.reporting;

import com.yammer.metrics.core.VirtualMachineMetrics;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An HTTP servlet which outputs a {@code text/plain} dump of all threads in the VM. Only responds
 * to {@code GET} requests.
 */
public class ThreadDumpServlet extends HttpServlet {
    private static final String CONTENT_TYPE = "text/plain";

    private final VirtualMachineMetrics vm;

    /**
     * Creates a new {@link ThreadDumpServlet}.
     */
    public ThreadDumpServlet() {
        this(VirtualMachineMetrics.getInstance());
    }

    /**
     * Creates a new {@link ThreadDumpServlet} with the given {@link VirtualMachineMetrics}
     * instance.
     *
     * @param vm    a {@link VirtualMachineMetrics} instance
     */
    public ThreadDumpServlet(VirtualMachineMetrics vm) {
        this.vm = vm;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENT_TYPE);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        final OutputStream output = resp.getOutputStream();
        try {
            vm.threadDump(output);
        } finally {
            output.close();
        }
    }
}
