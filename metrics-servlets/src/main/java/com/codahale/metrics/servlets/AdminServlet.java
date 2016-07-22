package com.codahale.metrics.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminServlet extends HttpServlet {
    public static final String DEFAULT_HEALTHCHECK_URI = "/healthcheck";
    public static final String DEFAULT_METRICS_URI = "/metrics";
    public static final String DEFAULT_PING_URI = "/ping";
    public static final String DEFAULT_THREADS_URI = "/threads";
    public static final String DEFAULT_CPU_PROFILE_URI = "/pprof";

    public static final String METRICS_URI_PARAM_KEY = "metrics-uri";
    public static final String PING_URI_PARAM_KEY = "ping-uri";
    public static final String THREADS_URI_PARAM_KEY = "threads-uri";
    public static final String HEALTHCHECK_URI_PARAM_KEY = "healthcheck-uri";
    public static final String SERVICE_NAME_PARAM_KEY= "service-name";
    public static final String CPU_PROFILE_URI_PARAM_KEY = "cpu-profile-uri";

    public static final String ADDITIONAL_ADMIN_ELEMENTS = AdminServlet.class.getCanonicalName() + ".additionalAdminElements";
    public static final String EXCLUDED_ADMIN_ELEMENTS = AdminServlet.class.getCanonicalName() + ".excludedAdminElements";
    
    private static final String CONTENT_TYPE = "text/html";
    private static final long serialVersionUID = -2850794040708785318L;

    private transient String metricsUri;
    private transient String pingUri;
    private transient String threadsUri;
    private transient String healthcheckUri;
    private transient String cpuprofileUri;
    private transient String serviceName;
    private transient List<AdminServletElement> adminServletElements;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        adminServletElements = new ArrayList<AdminServletElement>();

        addDefaultAdminElements(config);
        addAdditionalAdminElements(config);

        this.serviceName = getParam(config.getInitParameter(SERVICE_NAME_PARAM_KEY), null);
        
        initAllAdminElements(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getContextPath() + req.getServletPath();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setContentType(CONTENT_TYPE);
        final PrintWriter writer = resp.getWriter();
        writeBody(writer, path);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getPathInfo();
        if (uri == null || uri.equals("/")) {
            super.service(req, resp);
        } else {
            for (AdminServletElement adminServletElement : adminServletElements) {
                if (uri.equals(adminServletElement.getUri())) {
                    adminServletElement.getServlet().service(req, resp);
                    return;
                }
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void addDefaultAdminElements(ServletConfig config) {
        List<String> excludedAdminElements = getExcludedAdminElements(config);
        addMetricsIfNotExcluded(config, excludedAdminElements);
        addPingIfNotExcluded(config, excludedAdminElements);
        addThreadsIfNotExcluded(config, excludedAdminElements);
        addHealthChecksIfNotExcluded(config, excludedAdminElements);
        addCpuProfileIfNotExcluded(config, excludedAdminElements);
    }

    private List<String> getExcludedAdminElements(ServletConfig config) {
        Object exclutions = config.getServletContext().getAttribute(EXCLUDED_ADMIN_ELEMENTS);
        List<String> excludedAdminElements = exclutions instanceof List ? (List<String>) exclutions : Collections.<String>emptyList();
        return excludedAdminElements;
    }

    private void addMetricsIfNotExcluded(ServletConfig config, List<String> excludedAdminElements) {
        if(! excludedAdminElements.contains(MetricsServlet.class.getCanonicalName())) {
            this.metricsUri = getParam(config.getInitParameter(METRICS_URI_PARAM_KEY), DEFAULT_METRICS_URI);
            adminServletElements.add(
                    AdminServletElement.onPath(metricsUri).forServlet(new MetricsServlet())
                            .addLink(Link.to(metricsUri + "?pretty=true").withLabel("Metrics").build()).build());
        }
    }

    private void addPingIfNotExcluded(ServletConfig config, List<String> excludedAdminElements) {
        if(! excludedAdminElements.contains(PingServlet.class.getCanonicalName())) {
            this.pingUri = getParam(config.getInitParameter(PING_URI_PARAM_KEY), DEFAULT_PING_URI);
            adminServletElements.add(
                    AdminServletElement.onPath(pingUri).forServlet(new PingServlet())
                            .addLink(Link.to(pingUri).withLabel("Ping").build()).build());
        }
    }

    private void addThreadsIfNotExcluded(ServletConfig config, List<String> excludedAdminElements) {
        if(! excludedAdminElements.contains(ThreadDumpServlet.class.getCanonicalName())) {
            this.threadsUri = getParam(config.getInitParameter(THREADS_URI_PARAM_KEY), DEFAULT_THREADS_URI);
            adminServletElements.add(
                    AdminServletElement.onPath(threadsUri).forServlet(new ThreadDumpServlet())
                            .addLink(Link.to(threadsUri).withLabel("Threads").build()).build());
        }
    }

    private void addHealthChecksIfNotExcluded(ServletConfig config, List<String> excludedAdminElements) {
        if(! excludedAdminElements.contains(HealthCheckServlet.class.getCanonicalName())) {
            this.healthcheckUri = getParam(config.getInitParameter(HEALTHCHECK_URI_PARAM_KEY), DEFAULT_HEALTHCHECK_URI);
            adminServletElements.add(
                    AdminServletElement.onPath(healthcheckUri).forServlet(new HealthCheckServlet())
                            .addLink(Link.to(healthcheckUri + "?pretty=true").withLabel("Healthcheck").build()).build());
        }
    }

    private void addCpuProfileIfNotExcluded(ServletConfig config, List<String> excludedAdminElements) {
        if(! excludedAdminElements.contains(CpuProfileServlet.class.getCanonicalName())) {
            this.cpuprofileUri = getParam(config.getInitParameter(CPU_PROFILE_URI_PARAM_KEY), DEFAULT_CPU_PROFILE_URI);
            adminServletElements.add(
                    AdminServletElement.onPath(cpuprofileUri).forServlet(new CpuProfileServlet())
                            .addLink(Link.to(cpuprofileUri).withLabel("CPU Profile").build())
                            .addLink(Link.to(cpuprofileUri + "?state=blocked").withLabel("CPU Contention").build()).build());
        }
    }

    private void addAdditionalAdminElements(ServletConfig config) {
        Object additionalAdminElements = config.getServletContext().getAttribute(ADDITIONAL_ADMIN_ELEMENTS);
        if(additionalAdminElements instanceof List) {
            adminServletElements.addAll((List<AdminServletElement>) additionalAdminElements);
        }
    }

    private void initAllAdminElements(ServletConfig config) throws ServletException {
        for (AdminServletElement adminServletElement : adminServletElements) {
            adminServletElement.getServlet().init(config);
        }
    }

    private static String getParam(String initParam, String defaultValue) {
        return initParam == null ? defaultValue : initParam;
    }

    private void writeBody(final PrintWriter writer, final String path) {
        try {
            writer.println(Template.HEAD.fillWith(serviceName == null ? "" : " (" + serviceName + ")"));
            for (AdminServletElement adminServletElement : adminServletElements) {
                for(Link link : adminServletElement.getLinks()) {
                    writer.println(Template.LINE.fillWith(path, link.getPath(), link.getLabel()));
                }
            }
            writer.println(Template.TAIL.fillWith());
        } finally {
            writer.close();
        }
    }
    
    private enum Template {
        HEAD("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"%n" +
             "        \"http://www.w3.org/TR/html4/loose.dtd\">%n" +
             "<html>%n" +
             "<head>%n" +
             "  <title>Metrics{0}</title>%n" +
             "</head>%n" +
             "<body>%n" +
             "  <h1>Operational Menu{0}</h1>%n" +
             "  <ul>"),
        LINE("    <li><a href=\"{0}{1}\">{2}</a></li>"),
        TAIL("  </ul>%n" +
             "</body>%n" +
             "</html>");
        
        private final String template;

        private Template(String template) {
            this.template = String.format(template);
        }
        
        public String fillWith(Object... arguments) {
            return MessageFormat.format(template, arguments);
        }
    }
}
