package io.dropwizard.metrics.servlets;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

public class AdminServlet extends HttpServlet {
    public static final String DEFAULT_HEALTHCHECK_URI = "/healthcheck";
    public static final String DEFAULT_METRICS_URI = "/metrics";
    public static final String DEFAULT_PING_URI = "/ping";
    public static final String DEFAULT_THREADS_URI = "/threads";
    public static final String DEFAULT_CPU_PROFILE_URI = "/pprof";

    public static final String METRICS_ENABLED_PARAM_KEY = "metrics-enabled";
    public static final String METRICS_URI_PARAM_KEY = "metrics-uri";
    public static final String PING_ENABLED_PARAM_KEY = "ping-enabled";
    public static final String PING_URI_PARAM_KEY = "ping-uri";
    public static final String THREADS_ENABLED_PARAM_KEY = "threads-enabled";
    public static final String THREADS_URI_PARAM_KEY = "threads-uri";
    public static final String HEALTHCHECK_ENABLED_PARAM_KEY = "healthcheck-enabled";
    public static final String HEALTHCHECK_URI_PARAM_KEY = "healthcheck-uri";
    public static final String SERVICE_NAME_PARAM_KEY = "service-name";
    public static final String CPU_PROFILE_ENABLED_PARAM_KEY = "cpu-profile-enabled";
    public static final String CPU_PROFILE_URI_PARAM_KEY = "cpu-profile-uri";

    private static final String BASE_TEMPLATE =
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"%n" +
                    "        \"http://www.w3.org/TR/html4/loose.dtd\">%n" +
                    "<html>%n" +
                    "<head>%n" +
                    "  <title>Metrics{10}</title>%n" +
                    "</head>%n" +
                    "<body>%n" +
                    "  <h1>Operational Menu{10}</h1>%n" +
                    "  <ul>%n" +
                    "%s" +
                    "  </ul>%n" +
                    "</body>%n" +
                    "</html>";
    private static final String METRICS_LINK = "    <li><a href=\"{0}{1}?pretty=true\">Metrics</a></li>%n";
    private static final String PING_LINK = "    <li><a href=\"{2}{3}\">Ping</a></li>%n" ;
    private static final String THREADS_LINK = "    <li><a href=\"{4}{5}\">Threads</a></li>%n" ;
    private static final String HEALTHCHECK_LINK = "    <li><a href=\"{6}{7}?pretty=true\">Healthcheck</a></li>%n" ;
    private static final String CPU_PROFILE_LINK = "    <li><a href=\"{8}{9}\">CPU Profile</a></li>%n" +
            "    <li><a href=\"{8}{9}?state=blocked\">CPU Contention</a></li>%n";


    private static final String CONTENT_TYPE = "text/html";
    private static final long serialVersionUID = -2850794040708785318L;

    private transient HealthCheckServlet healthCheckServlet;
    private transient MetricsServlet metricsServlet;
    private transient PingServlet pingServlet;
    private transient ThreadDumpServlet threadDumpServlet;
    private transient CpuProfileServlet cpuProfileServlet;
    private transient boolean metricsEnabled;
    private transient String metricsUri;
    private transient boolean pingEnabled;
    private transient String pingUri;
    private transient boolean threadsEnabled;
    private transient String threadsUri;
    private transient boolean healthcheckEnabled;
    private transient String healthcheckUri;
    private transient boolean cpuProfileEnabled;
    private transient String cpuProfileUri;
    private transient String serviceName;
    private transient String pageContentTemplate;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final ServletContext context = config.getServletContext();
        final StringBuilder servletLinks = new StringBuilder();

        this.metricsEnabled =
                Boolean.parseBoolean(getParam(context.getInitParameter(METRICS_ENABLED_PARAM_KEY), "true"));
        if (this.metricsEnabled) {
            servletLinks.append(METRICS_LINK);
        }
        this.metricsServlet = new MetricsServlet();
        metricsServlet.init(config);

        this.pingEnabled =
                Boolean.parseBoolean(getParam(context.getInitParameter(PING_ENABLED_PARAM_KEY), "true"));
        if (this.pingEnabled) {
            servletLinks.append(PING_LINK);
        }
        this.pingServlet = new PingServlet();
        pingServlet.init(config);

        this.threadsEnabled =
                Boolean.parseBoolean(getParam(context.getInitParameter(THREADS_ENABLED_PARAM_KEY), "true"));
        if (this.threadsEnabled) {
            servletLinks.append(THREADS_LINK);
        }
        this.threadDumpServlet = new ThreadDumpServlet();
        threadDumpServlet.init(config);

        this.healthcheckEnabled =
                Boolean.parseBoolean(getParam(context.getInitParameter(HEALTHCHECK_ENABLED_PARAM_KEY), "true"));
        if (this.healthcheckEnabled) {
            servletLinks.append(HEALTHCHECK_LINK);
        }
        this.healthCheckServlet = new HealthCheckServlet();
        healthCheckServlet.init(config);

        this.cpuProfileEnabled =
                Boolean.parseBoolean(getParam(context.getInitParameter(CPU_PROFILE_ENABLED_PARAM_KEY), "true"));
        if (this.cpuProfileEnabled) {
            servletLinks.append(CPU_PROFILE_LINK);
        }
        this.cpuProfileServlet = new CpuProfileServlet();
        cpuProfileServlet.init(config);

        pageContentTemplate = String.format(BASE_TEMPLATE, String.format(servletLinks.toString()));

        this.metricsUri = getParam(context.getInitParameter(METRICS_URI_PARAM_KEY), DEFAULT_METRICS_URI);
        this.pingUri = getParam(context.getInitParameter(PING_URI_PARAM_KEY), DEFAULT_PING_URI);
        this.threadsUri = getParam(context.getInitParameter(THREADS_URI_PARAM_KEY), DEFAULT_THREADS_URI);
        this.healthcheckUri = getParam(context.getInitParameter(HEALTHCHECK_URI_PARAM_KEY), DEFAULT_HEALTHCHECK_URI);
        this.cpuProfileUri = getParam(context.getInitParameter(CPU_PROFILE_URI_PARAM_KEY), DEFAULT_CPU_PROFILE_URI);
        this.serviceName = getParam(context.getInitParameter(SERVICE_NAME_PARAM_KEY), null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getContextPath() + req.getServletPath();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setContentType(CONTENT_TYPE);
        try (PrintWriter writer = resp.getWriter()) {
            writer.println(MessageFormat.format(pageContentTemplate, path, metricsUri, path, pingUri, path,
                    threadsUri, path, healthcheckUri, path, cpuProfileUri,
                    serviceName == null ? "" : " (" + serviceName + ")"));
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getPathInfo();
        if (uri == null || uri.equals("/")) {
            super.service(req, resp);
        } else if (uri.equals(healthcheckUri)) {
            if (healthcheckEnabled) {
                healthCheckServlet.service(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (uri.startsWith(metricsUri)) {
            if (metricsEnabled) {
                metricsServlet.service(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (uri.equals(pingUri)) {
            if (pingEnabled) {
                pingServlet.service(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (uri.equals(threadsUri)) {
            if (threadsEnabled) {
                threadDumpServlet.service(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (uri.equals(cpuProfileUri)) {
            if (cpuProfileEnabled) {
                cpuProfileServlet.service(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static String getParam(String initParam, String defaultValue) {
        return initParam == null ? defaultValue : initParam;
    }
}
