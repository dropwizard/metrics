package com.codahale.metrics.servlets;

import com.codahale.metrics.Clock;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckFilter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HealthCheckServletTest extends AbstractServletTest {

    private static final ZonedDateTime FIXED_TIME = ZonedDateTime.now();

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static final String EXPECTED_TIMESTAMP = DATE_TIME_FORMATTER.format(FIXED_TIME);

    private static final Clock FIXED_CLOCK = new Clock() {
        @Override
        public long getTick() {
            return 0L;
        }

        @Override
        public long getTime() {
            return FIXED_TIME.toInstant().toEpochMilli();
        }
    };

    private final HealthCheckRegistry registry = new HealthCheckRegistry();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(HealthCheckServlet.class, "/healthchecks");
        tester.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.registry", registry);
        tester.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.executor", threadPool);
        tester.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.mapper", mapper);
        tester.setAttribute("com.codahale.metrics.servlets.HealthCheckServlet.healthCheckFilter",
                (HealthCheckFilter) (name, healthCheck) -> !"filtered".equals(name));
    }

    @Before
    public void setUp() {
        request.setMethod("GET");
        request.setURI("/healthchecks");
        request.setVersion("HTTP/1.0");
    }

    @After
    public void tearDown() {
        threadPool.shutdown();
    }

    @Test
    public void returns501IfNoHealthChecksAreRegistered() throws Exception {
        processRequest();

        assertThat(response.getStatus()).isEqualTo(501);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualTo("application/json");
        assertThat(response.getContent()).isEqualTo("{}");
    }

    @Test
    public void returnsA200IfAllHealthChecksAreHealthy() throws Exception {
        registry.register("fun", new TestHealthCheck(() -> healthyResultWithMessage("whee")));

        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualTo("application/json");
        assertThat(response.getContent())
                .isEqualTo("{\"fun\":{\"healthy\":true,\"message\":\"whee\",\"duration\":0,\"timestamp\":\"" +
                        EXPECTED_TIMESTAMP +
                        "\"}}");
    }

    @Test
    public void returnsASubsetOfHealthChecksIfFiltered() throws Exception {
        registry.register("fun", new TestHealthCheck(() -> healthyResultWithMessage("whee")));
        registry.register("filtered", new TestHealthCheck(() -> unhealthyResultWithMessage("whee")));

        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualTo("application/json");
        assertThat(response.getContent())
                .isEqualTo("{\"fun\":{\"healthy\":true,\"message\":\"whee\",\"duration\":0,\"timestamp\":\"" +
                        EXPECTED_TIMESTAMP +
                        "\"}}");
    }

    @Test
    public void returnsA500IfAnyHealthChecksAreUnhealthy() throws Exception {
        registry.register("fun", new TestHealthCheck(() -> healthyResultWithMessage("whee")));
        registry.register("notFun", new TestHealthCheck(() -> unhealthyResultWithMessage("whee")));

        processRequest();

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualTo("application/json");
        assertThat(response.getContent()).contains(
                        "{\"fun\":{\"healthy\":true,\"message\":\"whee\",\"duration\":0,\"timestamp\":\"" + EXPECTED_TIMESTAMP + "\"}",
                        ",\"notFun\":{\"healthy\":false,\"message\":\"whee\",\"duration\":0,\"timestamp\":\"" + EXPECTED_TIMESTAMP + "\"}}");
    }

    @Test
    public void returnsA200IfAnyHealthChecksAreUnhealthyAndHttpStatusIndicatorIsDisabled() throws Exception {
        registry.register("fun", new TestHealthCheck(() -> healthyResultWithMessage("whee")));
        registry.register("notFun", new TestHealthCheck(() -> unhealthyResultWithMessage("whee")));
        request.setURI("/healthchecks?httpStatusIndicator=false");

        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualTo("application/json");
        assertThat(response.getContent()).contains(
                "{\"fun\":{\"healthy\":true,\"message\":\"whee\",\"duration\":0,\"timestamp\":\"" + EXPECTED_TIMESTAMP + "\"}",
                ",\"notFun\":{\"healthy\":false,\"message\":\"whee\",\"duration\":0,\"timestamp\":\"" + EXPECTED_TIMESTAMP + "\"}}");
    }

    @Test
    public void optionallyPrettyPrintsTheJson() throws Exception {
        registry.register("fun", new TestHealthCheck(() -> healthyResultWithMessage("foo bar 123")));

        request.setURI("/healthchecks?pretty=true");

        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualTo("application/json");
        assertThat(response.getContent())
                .isEqualTo(String.format("{%n" +
                        "  \"fun\" : {%n" +
                        "    \"healthy\" : true,%n" +
                        "    \"message\" : \"foo bar 123\",%n" +
                        "    \"duration\" : 0,%n" +
                        "    \"timestamp\" : \"" + EXPECTED_TIMESTAMP + "\"" +
                        "%n  }%n}"));
    }

    private static HealthCheck.Result healthyResultWithMessage(String message) {
        return HealthCheck.Result.builder()
                .healthy()
                .withMessage(message)
                .usingClock(FIXED_CLOCK)
                .build();
    }

    private static HealthCheck.Result unhealthyResultWithMessage(String message) {
        return HealthCheck.Result.builder()
                .unhealthy()
                .withMessage(message)
                .usingClock(FIXED_CLOCK)
                .build();
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
        verify(servletContext, never()).getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY);
    }

    @Test
    public void constructorWithRegistryAsArgumentUsesServletConfigWhenNull() throws Exception {
        final HealthCheckRegistry healthCheckRegistry = mock(HealthCheckRegistry.class);
        final ServletContext servletContext = mock(ServletContext.class);
        final ServletConfig servletConfig = mock(ServletConfig.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY))
                .thenReturn(healthCheckRegistry);

        final HealthCheckServlet healthCheckServlet = new HealthCheckServlet(null);
        healthCheckServlet.init(servletConfig);

        verify(servletConfig, times(1)).getServletContext();
        verify(servletContext, times(1)).getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY);
    }

    @Test(expected = ServletException.class)
    public void constructorWithRegistryAsArgumentUsesServletConfigWhenNullButWrongTypeInContext() throws Exception {
        final ServletContext servletContext = mock(ServletContext.class);
        final ServletConfig servletConfig = mock(ServletConfig.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY))
                .thenReturn("IRELLEVANT_STRING");

        final HealthCheckServlet healthCheckServlet = new HealthCheckServlet(null);
        healthCheckServlet.init(servletConfig);
    }

    @Test
    public void constructorWithObjectMapperAsArgumentUsesServletConfigWhenNullButWrongTypeInContext() throws Exception {
        final ServletContext servletContext = mock(ServletContext.class);
        final ServletConfig servletConfig = mock(ServletConfig.class);
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY)).thenReturn(registry);
        when(servletContext.getAttribute(HealthCheckServlet.HEALTH_CHECK_MAPPER)).thenReturn("IRELLEVANT_STRING");

        final HealthCheckServlet healthCheckServlet = new HealthCheckServlet(null);
        healthCheckServlet.init(servletConfig);

        assertThat(healthCheckServlet.getMapper())
                .isNotNull()
                .isInstanceOf(ObjectMapper.class);
    }

    static class TestHealthCheck extends HealthCheck {
        private final Callable<Result> check;

        public TestHealthCheck(Callable<Result> check) {
            this.check = check;
        }

        @Override
        protected Result check() throws Exception {
            return check.call();
        }

        @Override
        protected Clock clock() {
            return FIXED_CLOCK;
        }
    }
}
