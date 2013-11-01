package com.codahale.metrics.servlets;

import javax.servlet.http.HttpServlet;

public abstract class AppDiagnosticBaseServlet extends HttpServlet {

    public static final String SERVLETS_REGISTRY = AppDiagnosticBaseServlet.class.getCanonicalName() + ".registry";

    public abstract String uri();
    public abstract String displayName();
    public abstract boolean supportsPrettyPrint();
}
