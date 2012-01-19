package com.yammer.metrics.reporting;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * An HTTP servlet which outputs a {@code text/plain} {@code "pong"} response.
 */
public class PingServlet extends HttpServlet {
    private static final String CONTENT_TYPE = "text/plain";
    private static final String CONTENT = "pong";

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setContentType(CONTENT_TYPE);
        final PrintWriter writer = resp.getWriter();
        try {
            writer.println(CONTENT);
        } finally {
            writer.close();
        }
    }
}
