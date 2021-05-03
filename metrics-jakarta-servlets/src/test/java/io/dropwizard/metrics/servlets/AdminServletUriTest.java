package io.dropwizard.metrics.servlets;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminServletUriTest extends AbstractServletTest {
    private final MetricRegistry registry = new MetricRegistry();
    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    @Override
    protected void setUp(ServletTester tester) {
        tester.setContextPath("/context");

        tester.setAttribute("io.dropwizard.metrics.servlets.MetricsServlet.registry", registry);
        tester.setAttribute("io.dropwizard.metrics.servlets.HealthCheckServlet.registry", healthCheckRegistry);
        tester.setInitParameter("metrics-uri", "/metrics-test");
        tester.setInitParameter("ping-uri", "/ping-test");
        tester.setInitParameter("threads-uri", "/threads-test");
        tester.setInitParameter("healthcheck-uri", "/healthcheck-test");
        tester.setInitParameter("cpu-profile-uri", "/pprof-test");
        tester.addServlet(AdminServlet.class, "/admin");
    }

    @Before
    public void setUp() {
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
                .isEqualTo(String.format(
                        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"%n" +
                                "        \"http://www.w3.org/TR/html4/loose.dtd\">%n" +
                                "<html>%n" +
                                "<head>%n" +
                                "  <title>Metrics</title>%n" +
                                "</head>%n" +
                                "<body>%n" +
                                "  <h1>Operational Menu</h1>%n" +
                                "  <ul>%n" +
                                "    <li><a href=\"/context/admin/metrics-test?pretty=true\">Metrics</a></li>%n" +
                                "    <li><a href=\"/context/admin/ping-test\">Ping</a></li>%n" +
                                "    <li><a href=\"/context/admin/threads-test\">Threads</a></li>%n" +
                                "    <li><a href=\"/context/admin/healthcheck-test?pretty=true\">Healthcheck</a></li>%n" +
                                "    <li><a href=\"/context/admin/pprof-test\">CPU Profile</a></li>%n" +
                                "    <li><a href=\"/context/admin/pprof-test?state=blocked\">CPU Contention</a></li>%n" +
                                "  </ul>%n" +
                                "</body>%n" +
                                "</html>%n"
                ));
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo("text/html;charset=UTF-8");
    }
}
