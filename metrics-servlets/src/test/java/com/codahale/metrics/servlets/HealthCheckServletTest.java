package com.codahale.metrics.servlets;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import static org.fest.assertions.api.Assertions.assertThat;

public class HealthCheckServletTest extends AbstractServletTest {
    private final HealthCheckRegistry registry = new HealthCheckRegistry();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    @Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(HealthCheckServlet.class, "/healthchecks");
        tester.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.registry", registry);
        tester.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.executor", threadPool);
    }

    @Before
    public void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI("/healthchecks");
        request.setVersion("HTTP/1.0");
    }

    @After
    public void tearDown() throws Exception {
        threadPool.shutdown();
    }

    @Test
    public void returns501IfNoHealthChecksAreRegistered() throws Exception {
        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(501);
        assertThat(response.getContent())
                .isEqualTo("{}");
        assertThat(response.getContentType())
                .isEqualTo("application/json");
    }

    @Test
    public void returnsA200IfAllHealthChecksAreHealthy() throws Exception {
        registry.register("fun", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy("whee");
            }
        });

        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("{\"fun\":{\"healthy\":true,\"message\":\"whee\"}}");
        assertThat(response.getContentType())
                .isEqualTo("application/json");
    }

    @Test
    public void returnsA500IfAnyHealthChecksAreUnhealthy() throws Exception {
        registry.register("fun", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy("whee");
            }
        });

        registry.register("notFun", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.unhealthy("whee");
            }
        });

        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(500);
        assertThat(response.getContent())
                .isEqualTo("{\"fun\":{\"healthy\":true,\"message\":\"whee\"},\"notFun\":{\"healthy\":false,\"message\":\"whee\"}}");
        assertThat(response.getContentType())
                .isEqualTo("application/json");
    }

    @Test
    public void optionallyPrettyPrintsTheJson() throws Exception {
        registry.register("fun", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy("whee");
            }
        });

        request.setURI("/healthchecks?pretty=true");

        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo(String.format("{%n" +
                                                 "  \"fun\" : {%n" +
                                                 "    \"healthy\" : true,%n" +
                                                 "    \"message\" : \"whee\"%n" +
                                                 "  }%n" +
                                                 "}"));
        assertThat(response.getContentType())
                .isEqualTo("application/json");
    }

    @Test
    public void constructorWithRegistryAsArgumentIsUsedInPreferenceOverServletConfig() throws Exception {
        final HealthCheckRegistry healthCheckRegistry = mock(HealthCheckRegistry.class);
        final ServletContext servletContext = mock(ServletContext.class);
        final ServletConfig servletConfig = mock(ServletConfig.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
 
        final HealthCheckServlet healthCheckServlet = new HealthCheckServlet(healthCheckRegistry);
        healthCheckServlet.init(servletConfig);
 
        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, never()).getAttribute(eq(HealthCheckServlet.HEALTH_CHECK_REGISTRY));
    }

    @Test
    public void constructorWithRegistryAsArgumentUsesServletConfigWhenNull() throws Exception {
        final HealthCheckRegistry healthCheckRegistry = mock(HealthCheckRegistry.class);
        final ServletContext servletContext = mock(ServletContext.class);
        final ServletConfig servletConfig = mock(ServletConfig.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(eq(HealthCheckServlet.HEALTH_CHECK_REGISTRY)))
       	    .thenReturn(healthCheckRegistry);
 
        final HealthCheckServlet healthCheckServlet = new HealthCheckServlet(null);
        healthCheckServlet.init(servletConfig);
 
        verify(servletConfig, times(2)).getServletContext();
        verify(servletContext, times(1)).getAttribute(eq(HealthCheckServlet.HEALTH_CHECK_REGISTRY));
    }

    @Test(expected = ServletException.class)
    public void constructorWithRegistryAsArgumentUsesServletConfigWhenNullButWrongTypeInContext() throws Exception {
        final ServletContext servletContext = mock(ServletContext.class);
        final ServletConfig servletConfig = mock(ServletConfig.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(eq(HealthCheckServlet.HEALTH_CHECK_REGISTRY)))
            .thenReturn("IRELLEVANT_STRING");
 
        final HealthCheckServlet healthCheckServlet = new HealthCheckServlet(null);
        healthCheckServlet.init(servletConfig);
    }
}
