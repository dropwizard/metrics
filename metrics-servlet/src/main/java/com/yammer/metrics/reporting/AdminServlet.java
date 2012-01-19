package com.yammer.metrics.reporting;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1363903248255082791L;

    private static final String TEMPLATE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
                                           "        \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                                           "<html>\n" +
                                           "<head>\n" +
                                           "  <title>Metrics</title>\n" +
                                           "</head>\n" +
                                           "<body>\n" +
                                           "  <h1>Operational Menu</h1>\n" +
                                           "  <ul>\n" +
                                           "    <li><a href=\"{0}{1}?pretty=true\">Metrics</a></li>\n" +
                                           "    <li><a href=\"{2}{3}\">Ping</a></li>\n" +
                                           "    <li><a href=\"{4}{5}\">Threads</a></li>\n" +
                                           "    <li><a href=\"{6}{7}\">Healthcheck</a></li>\n" +
                                           "  </ul>\n" +
                                           "</body>\n" +
                                           "</html>";

    public static final String DEFAULT_HEALTHCHECK_URI = "/healthcheck";
    public static final String DEFAULT_METRICS_URI = "/metrics";
    public static final String DEFAULT_PING_URI = "/ping";
    public static final String DEFAULT_THREADS_URI = "/threads";
    private static final String CONTENT_TYPE = "text/html";

    private final HealthCheckServlet healthCheckServlet;
    private final MetricsServlet metricsServlet;
    private final PingServlet pingServlet;
    private final ThreadDumpServlet threadDumpServlet;

    private String metricsUri, pingUri, threadsUri, healthcheckUri, contextPath;

    public AdminServlet() {
        this(new HealthCheckServlet(), new MetricsServlet(), new PingServlet(),
             new ThreadDumpServlet(), DEFAULT_HEALTHCHECK_URI, DEFAULT_METRICS_URI,
             DEFAULT_PING_URI, DEFAULT_THREADS_URI);
    }

    public AdminServlet(HealthCheckServlet healthCheckServlet,
                        MetricsServlet metricsServlet,
                        PingServlet pingServlet,
                        ThreadDumpServlet threadDumpServlet,
                        String healthcheckUri,
                        String metricsUri,
                        String pingUri,
                        String threadsUri) {
        this.healthCheckServlet = healthCheckServlet;
        this.metricsServlet = metricsServlet;
        this.pingServlet = pingServlet;
        this.threadDumpServlet = threadDumpServlet;

        this.metricsUri = metricsUri;
        this.pingUri = pingUri;
        this.threadsUri = threadsUri;
        this.healthcheckUri = healthcheckUri;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        healthCheckServlet.init(config);
        metricsServlet.init(config);
        pingServlet.init(config);
        threadDumpServlet.init(config);

        final ServletContext context = config.getServletContext();
        this.contextPath = context.getContextPath();
        this.metricsUri = getParam(config.getInitParameter("metrics-uri"), this.metricsUri);
        this.pingUri = getParam(config.getInitParameter("ping-uri"), this.pingUri);
        this.threadsUri = getParam(config.getInitParameter("threads-uri"), this.threadsUri);
        this.healthcheckUri = getParam(config.getInitParameter("healthcheck-uri"), this.healthcheckUri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        final String uri = req.getPathInfo();
        final String path = this.contextPath + req.getServletPath();
        if (uri == null || uri.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENT_TYPE);
            final PrintWriter writer = resp.getWriter();
            try {
                writer.println(MessageFormat.format(TEMPLATE, path, metricsUri, path, pingUri, path,
                                                    threadsUri, path, healthcheckUri));
            } finally {
                writer.close();
            }
        } else if (uri.equals(healthcheckUri)) {
            healthCheckServlet.service(req, resp);
        } else if (uri.startsWith(metricsUri)) {
            metricsServlet.service(req, resp);
        } else if (uri.equals(pingUri)) {
            pingServlet.service(req, resp);
        } else if (uri.equals(threadsUri)) {
            threadDumpServlet.service(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static String getParam(String initParam, String defaultValue) {
        return initParam == null ? defaultValue : initParam;
    }
}
