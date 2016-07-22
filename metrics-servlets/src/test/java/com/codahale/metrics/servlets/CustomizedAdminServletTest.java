package com.codahale.metrics.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

public class CustomizedAdminServletTest extends AbstractServletTest {

    private final MetricRegistry registry = new MetricRegistry();
    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    @Override
    protected void setUp(ServletTester tester) {
        tester.setContextPath("/context");

        List<AdminServletElement> additionalAdminElements = newArrayList(
                AdminServletElement.onPath("/myCustomAdminElement").forServlet(new TestServlet())
                .addLink(Link.to("/myCustomAdminElement?qp=set").withLabel("Custom Element").build()).build());
        
        List<String> excludedAdminElements = newArrayList(HealthCheckServlet.class.getCanonicalName());

        tester.setAttribute("com.codahale.metrics.servlets.MetricsServlet.registry", registry);
        tester.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.registry", healthCheckRegistry);
        tester.setAttribute("com.codahale.metrics.servlets.AdminServlet.additionalAdminElements", additionalAdminElements);
        tester.setAttribute("com.codahale.metrics.servlets.AdminServlet.excludedAdminElements", excludedAdminElements);
        tester.addServlet(AdminServlet.class, "/admin/*");
    }

    @Before
    public void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/context/admin");
        request.setVersion("HTTP/1.0");
    }

    @Test
    public void containsCustomAdminElementLink() throws Exception {
        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("<li><a href=\"/context/admin/myCustomAdminElement?qp=set\">Custom Element</a></li>");
    }

    @Test
    public void doesNotAddExcludedHealthCheckElement() throws Exception {
        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .doesNotContain("/context/admin/healthcheck");
    }

    @Test
    public void servesCustomAdminElementServlet() throws Exception {
        request.setURI("/context/admin/myCustomAdminElement?qp=content");
        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("content\n");
    }

    private static class TestServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
            resp.setContentType("text/plain");
            final PrintWriter writer = resp.getWriter();
            try {
                writer.println(req.getParameter("qp"));
            } finally {
                writer.close();
            }
        }
    }
}
