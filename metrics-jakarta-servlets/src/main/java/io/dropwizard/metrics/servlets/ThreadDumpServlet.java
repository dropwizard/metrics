package io.dropwizard.metrics.servlets;

import com.codahale.metrics.jvm.ThreadDump;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;

/**
 * An HTTP servlets which outputs a {@code text/plain} dump of all threads in
 * the VM. Only responds to {@code GET} requests.
 */
public class ThreadDumpServlet extends HttpServlet {

    private static final long serialVersionUID = -2690343532336103046L;
    private static final String CONTENT_TYPE = "text/plain";

    private transient ThreadDump threadDump;

    @Override
    public void init() throws ServletException {
        try {
            // Some PaaS like Google App Engine blacklist java.lang.managament
            this.threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
        } catch (NoClassDefFoundError ncdfe) {
            this.threadDump = null; // we won't be able to provide thread dump
        }
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        final boolean includeMonitors = getParam(req.getParameter("monitors"), true);
        final boolean includeSynchronizers = getParam(req.getParameter("synchronizers"), true);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENT_TYPE);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        if (threadDump == null) {
            resp.getWriter().println("Sorry your runtime environment does not allow to dump threads.");
            return;
        }
        try (OutputStream output = resp.getOutputStream()) {
            threadDump.dump(includeMonitors, includeSynchronizers, output);
        }
    }

    private static Boolean getParam(String initParam, boolean defaultValue) {
        return initParam == null ? defaultValue : Boolean.parseBoolean(initParam);
    }
}
