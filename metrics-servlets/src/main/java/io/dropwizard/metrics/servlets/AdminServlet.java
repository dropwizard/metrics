package io.dropwizard.metrics.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

public class AdminServlet extends HttpServlet {
    public static final String DEFAULT_HEALTHCHECK_URI = "/healthcheck";
    public static final String DEFAULT_METRICS_URI = "/metrics";
    public static final String DEFAULT_PING_URI = "/ping";
    public static final String DEFAULT_THREADS_URI = "/threads";
    public static final String DEFAULT_MANIFEST_URI = "/manifest";
    public static final String DEFAULT_CPU_PROFILE_URI = "/pprof";

    public static final String METRICS_URI_PARAM_KEY = "metrics-uri";
    public static final String PING_URI_PARAM_KEY = "ping-uri";
    public static final String THREADS_URI_PARAM_KEY = "threads-uri";
    public static final String HEALTHCHECK_URI_PARAM_KEY = "healthcheck-uri";
    public static final String MANIFEST_URI_PARAM_KEY = "manifest-uri";
    public static final String SERVICE_NAME_PARAM_KEY= "service-name";
    public static final String CPU_PROFILE_URI_PARAM_KEY = "cpu-profile-uri";

    private static final String TEMPLATE = String.format(
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"%n" +
                    "        \"http://www.w3.org/TR/html4/loose.dtd\">%n" +
                    "<html>%n" +
                    "<head>%n" +
                    "  <title>Metrics{11}</title>%n" +
                    "</head>%n" +
                    "<body>%n" +
                    "  <h1>Operational Menu{11}</h1>%n" +
                    "  <ul>%n" +
                    "    <li><a href=\"{0}{1}?pretty=true\">Metrics</a></li>%n" +
                    "    <li><a href=\"{2}{3}\">Ping</a></li>%n" +
                    "    <li><a href=\"{4}{5}\">Threads</a></li>%n" +
                    "    <li><a href=\"{6}{7}?pretty=true\">Healthcheck</a></li>%n" +
                    "    <li><a href=\"{8}{9}\">Manifest</a></li>%n" +
                    "    <li><a href=\"{8}{10}\">CPU Profile</a></li>%n" +
                    "    <li><a href=\"{8}{10}?state=blocked\">CPU Contention</a></li>%n" +
                    "  </ul>%n" +
                    "</body>%n" +
                    "</html>"
    );
    private static final String CONTENT_TYPE = "text/html";
    private static final long serialVersionUID = -2850794040708785318L;

    private transient HealthCheckServlet healthCheckServlet;
    private transient MetricsServlet metricsServlet;
    private transient PingServlet pingServlet;
    private transient ThreadDumpServlet threadDumpServlet;
    private transient ManifestServlet manifestServlet;
    private transient CpuProfileServlet cpuProfileServlet;
    private transient String metricsUri;
    private transient String pingUri;
    private transient String threadsUri;
    private transient String healthcheckUri;
    private transient String manifestUri;
    private transient String cpuprofileUri;
    private transient String serviceName;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.healthCheckServlet = new HealthCheckServlet();
        healthCheckServlet.init(config);

        this.metricsServlet = new MetricsServlet();
        metricsServlet.init(config);

        this.pingServlet = new PingServlet();
        pingServlet.init(config);

        this.threadDumpServlet = new ThreadDumpServlet();
        threadDumpServlet.init(config);

        this.manifestServlet = new ManifestServlet();
        manifestServlet.init(config);
        this.cpuProfileServlet = new CpuProfileServlet();
        cpuProfileServlet.init(config);

        this.metricsUri = getParam(config.getInitParameter(METRICS_URI_PARAM_KEY), DEFAULT_METRICS_URI);
        this.pingUri = getParam(config.getInitParameter(PING_URI_PARAM_KEY), DEFAULT_PING_URI);
        this.threadsUri = getParam(config.getInitParameter(THREADS_URI_PARAM_KEY), DEFAULT_THREADS_URI);
        this.healthcheckUri = getParam(config.getInitParameter(HEALTHCHECK_URI_PARAM_KEY), DEFAULT_HEALTHCHECK_URI);
        this.manifestUri = getParam(config.getInitParameter(MANIFEST_URI_PARAM_KEY), DEFAULT_MANIFEST_URI);
        this.cpuprofileUri = getParam(config.getInitParameter(CPU_PROFILE_URI_PARAM_KEY), DEFAULT_CPU_PROFILE_URI);
        this.serviceName = getParam(config.getInitParameter(SERVICE_NAME_PARAM_KEY), null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getContextPath() + req.getServletPath();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setContentType(CONTENT_TYPE);
        final PrintWriter writer = resp.getWriter();
        try {
            writer.println(MessageFormat.format(TEMPLATE, path, metricsUri, path, pingUri, path,
                                                threadsUri, path, healthcheckUri, path, manifestUri, cpuprofileUri,
                                                serviceName == null ? "" : " (" + serviceName + ")"));
        } finally {
            writer.close();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getPathInfo();
        if (uri == null || uri.equals("/")) {
            super.service(req, resp);
        } else if (uri.equals(healthcheckUri)) {
            healthCheckServlet.service(req, resp);
        } else if (uri.startsWith(metricsUri)) {
            metricsServlet.service(req, resp);
        } else if (uri.equals(pingUri)) {
            pingServlet.service(req, resp);
        } else if (uri.equals(threadsUri)) {
            threadDumpServlet.service(req, resp);
        } else if (uri.equals(manifestUri)) {
            manifestServlet.service(req, resp);
        } else if (uri.equals(cpuprofileUri)) {
            cpuProfileServlet.service(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static String getParam(String initParam, String defaultValue) {
        return initParam == null ? defaultValue : initParam;
    }
}
