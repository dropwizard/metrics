package com.yammer.metrics.servlets;

import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.health.HealthCheckRegistry;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class AdminServletTest extends AbstractServletTest {
    private final MetricRegistry registry = new MetricRegistry();
    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    @Override
    protected void setUp(ServletTester tester) {
        tester.setContextPath("/context");

        tester.setAttribute("com.yammer.metrics.servlets.MetricsServlet.registry", registry);
        tester.setAttribute("com.yammer.metrics.servlets.HealthCheckServlet.registry", healthCheckRegistry);
        tester.addServlet(AdminServlet.class, "/admin");
    }

    @Before
    public void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/context/admin");
        request.setVersion("HTTP/1.0");
    }

    @Test
    public void returnsA200() throws Exception {
        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
                                   "        \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                                   "<html>\n" +
                                   "<head>\n" +
                                   "  <title>Metrics</title>\n" +
                                   "</head>\n" +
                                   "<body>\n" +
                                   "  <h1>Operational Menu</h1>\n" +
                                   "  <ul>\n" +
                                   "    <li><a href=\"/context/admin/metrics?pretty=true\">Metrics</a></li>\n" +
                                   "    <li><a href=\"/context/admin/ping\">Ping</a></li>\n" +
                                   "    <li><a href=\"/context/admin/threads\">Threads</a></li>\n" +
                                   "    <li><a href=\"/context/admin/healthcheck\">Healthcheck</a></li>\n" +
                                   "  </ul>\n" +
                                   "</body>\n" +
                                   "</html>\n");
        assertThat(response.getContentType())
                .isEqualTo("text/html;charset=ISO-8859-1");
    }
}
