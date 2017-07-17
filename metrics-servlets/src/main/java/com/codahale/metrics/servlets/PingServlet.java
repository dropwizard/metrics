package com.codahale.metrics.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * An HTTP servlets which outputs a {@code text/plain} {@code "pong"} response.
 */
public class PingServlet extends HttpServlet {
    private static final long serialVersionUID = 3772654177231086757L;
    private static final String CONTENT_TYPE = "text/plain";
    private static final String CONTENT = "pong";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String NO_CACHE = "must-revalidate,no-cache,no-store";

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader(CACHE_CONTROL, NO_CACHE);
        resp.setContentType(CONTENT_TYPE);
        try (PrintWriter writer = resp.getWriter()) {
            writer.println(CONTENT);
        }
    }
}
