package com.codahale.metrics.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AdminServlet extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html";
    private static final long serialVersionUID = -2850794040708785318L;

    private transient AdminPage.Builder builder;// = new AdminPage.Builder();
    private transient AdminPage adminPage;
    private transient Map<String, AppDiagnosticBaseServlet> mapping;

    @Override
    public void init(ServletConfig config) throws ServletException {
        builder = new AdminPage.Builder();
        mapping = new TreeMap<String, AppDiagnosticBaseServlet>();
        List<? extends AppDiagnosticBaseServlet> servlets = getServlets(config);

        for (AppDiagnosticBaseServlet servlet : servlets) {
            servlet.init(config);
            mapping.put(servlet.uri(), servlet);
            builder.addItem(servlet.uri(), servlet.displayName(), servlet.supportsPrettyPrint());
        }

        adminPage = builder.build();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getContextPath() + req.getServletPath();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setContentType(CONTENT_TYPE);
        final PrintWriter writer = resp.getWriter();
        try {
            writer.println(adminPage.menu(path));
        } finally {
            writer.close();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getPathInfo();
        if (uri == null || uri.equals("/")) {
            super.service(req, resp);
        } else if (mapping.containsKey(uri)) {
            mapping.get(uri).service(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @SuppressWarnings("unchecked")
    private List<? extends AppDiagnosticBaseServlet> getServlets(ServletConfig config) {
        return (List<? extends AppDiagnosticBaseServlet>) config.getServletContext().getAttribute(AppDiagnosticBaseServlet.SERVLETS_REGISTRY);
    }
}
