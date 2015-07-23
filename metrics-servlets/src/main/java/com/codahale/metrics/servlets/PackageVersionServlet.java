package com.codahale.metrics.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An HTTP servlet which outputs a {@code text/plain} jar package version.
 * Only responds to {@code GET} requests.
 *
 * It can be useful to find out which package version is actually running.
 *
 * Make sure your service jar Manifest file contains "Implementation-Version" field.
 */
public class PackageVersionServlet extends HttpServlet {

    private static final long serialVersionUID = 5017319670694059252L;
    private static final String CONTENT_TYPE = "text/plain";

    private String implementationVersion;

    @Override
    public void init() throws ServletException {
        try {
            implementationVersion = getClass().getPackage().getImplementationVersion();
        } catch (Exception e) {
            implementationVersion = null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENT_TYPE);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.getWriter().println(implementationVersion);
        resp.getWriter().close();
    }
}
