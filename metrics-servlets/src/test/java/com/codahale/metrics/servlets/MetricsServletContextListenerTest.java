package com.codahale.metrics.servlets;

import com.codahale.metrics.Clock;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsServletContextListenerTest extends AbstractServletTest {
    private final Clock clock = mock(Clock.class);
    private final MetricRegistry registry = new MetricRegistry();
    private final String allowedOrigin = "some.other.origin";

    @Override
    protected void setUp(ServletTester tester) {
        tester.setAttribute("com.codahale.metrics.servlets.MetricsServlet.registry", registry);
        tester.addServlet(MetricsServlet.class, "/metrics");
        tester.getContext().addEventListener(new MetricsServlet.ContextListener() {
            @Override
            protected MetricRegistry getMetricRegistry() {
                return registry;
            }

            @Override
            protected TimeUnit getDurationUnit() {
                return TimeUnit.MILLISECONDS;
            }

            @Override
            protected TimeUnit getRateUnit() {
                return TimeUnit.MINUTES;
            }

            @Override
            protected String getAllowedOrigin() {
                return allowedOrigin;
            }
        });
    }

    @Before
    public void setUp() {
        // provide ticks for the setup (calls getTick 6 times). The serialization in the tests themselves
        // will call getTick again several times and always get the same value (the last specified here)
        when(clock.getTick()).thenReturn(100L, 100L, 200L, 300L, 300L, 400L);

        registry.register("g1", (Gauge<Long>) () -> 100L);
        registry.counter("c").inc();
        registry.histogram("h").update(1);
        registry.register("m", new Meter(clock)).mark();
        registry.register("t", new Timer(new ExponentiallyDecayingReservoir(), clock))
                .update(1, TimeUnit.SECONDS);

        request.setMethod("GET");
        request.setURI("/metrics");
        request.setVersion("HTTP/1.0");
    }

    @Test
    public void returnsA200() throws Exception {
        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.get("Access-Control-Allow-Origin"))
                .isEqualTo(allowedOrigin);
        assertThat(response.getContent())
                .isEqualTo("{" +
                        "\"version\":\"4.0.0\"," +
                        "\"gauges\":{" +
                        "\"g1\":{\"value\":100}" +
                        "}," +
                        "\"counters\":{" +
                        "\"c\":{\"count\":1}" +
                        "}," +
                        "\"histograms\":{" +
                        "\"h\":{\"count\":1,\"max\":1,\"mean\":1.0,\"min\":1,\"p50\":1.0,\"p75\":1.0,\"p95\":1.0,\"p98\":1.0,\"p99\":1.0,\"p999\":1.0,\"stddev\":0.0}" +
                        "}," +
                        "\"meters\":{" +
                        "\"m\":{\"count\":1,\"m15_rate\":0.0,\"m1_rate\":0.0,\"m5_rate\":0.0,\"mean_rate\":2.0E8,\"units\":\"events/minute\"}},\"timers\":{\"t\":{\"count\":1,\"max\":1000.0,\"mean\":1000.0,\"min\":1000.0,\"p50\":1000.0,\"p75\":1000.0,\"p95\":1000.0,\"p98\":1000.0,\"p99\":1000.0,\"p999\":1000.0,\"stddev\":0.0,\"m15_rate\":0.0,\"m1_rate\":0.0,\"m5_rate\":0.0,\"mean_rate\":6.0E8,\"duration_units\":\"milliseconds\",\"rate_units\":\"calls/minute\"}" +
                        "}" +
                        "}");
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo("application/json");
    }

    @Test
    public void optionallyPrettyPrintsTheJson() throws Exception {
        request.setURI("/metrics?pretty=true");

        processRequest();

        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.get("Access-Control-Allow-Origin"))
                .isEqualTo(allowedOrigin);
        assertThat(response.getContent())
                .isEqualTo(String.format("{%n" +
                        "  \"version\" : \"4.0.0\",%n" +
                        "  \"gauges\" : {%n" +
                        "    \"g1\" : {%n" +
                        "      \"value\" : 100%n" +
                        "    }%n" +
                        "  },%n" +
                        "  \"counters\" : {%n" +
                        "    \"c\" : {%n" +
                        "      \"count\" : 1%n" +
                        "    }%n" +
                        "  },%n" +
                        "  \"histograms\" : {%n" +
                        "    \"h\" : {%n" +
                        "      \"count\" : 1,%n" +
                        "      \"max\" : 1,%n" +
                        "      \"mean\" : 1.0,%n" +
                        "      \"min\" : 1,%n" +
                        "      \"p50\" : 1.0,%n" +
                        "      \"p75\" : 1.0,%n" +
                        "      \"p95\" : 1.0,%n" +
                        "      \"p98\" : 1.0,%n" +
                        "      \"p99\" : 1.0,%n" +
                        "      \"p999\" : 1.0,%n" +
                        "      \"stddev\" : 0.0%n" +
                        "    }%n" +
                        "  },%n" +
                        "  \"meters\" : {%n" +
                        "    \"m\" : {%n" +
                        "      \"count\" : 1,%n" +
                        "      \"m15_rate\" : 0.0,%n" +
                        "      \"m1_rate\" : 0.0,%n" +
                        "      \"m5_rate\" : 0.0,%n" +
                        "      \"mean_rate\" : 2.0E8,%n" +
                        "      \"units\" : \"events/minute\"%n" +
                        "    }%n" +
                        "  },%n" +
                        "  \"timers\" : {%n" +
                        "    \"t\" : {%n" +
                        "      \"count\" : 1,%n" +
                        "      \"max\" : 1000.0,%n" +
                        "      \"mean\" : 1000.0,%n" +
                        "      \"min\" : 1000.0,%n" +
                        "      \"p50\" : 1000.0,%n" +
                        "      \"p75\" : 1000.0,%n" +
                        "      \"p95\" : 1000.0,%n" +
                        "      \"p98\" : 1000.0,%n" +
                        "      \"p99\" : 1000.0,%n" +
                        "      \"p999\" : 1000.0,%n" +
                        "      \"stddev\" : 0.0,%n" +
                        "      \"m15_rate\" : 0.0,%n" +
                        "      \"m1_rate\" : 0.0,%n" +
                        "      \"m5_rate\" : 0.0,%n" +
                        "      \"mean_rate\" : 6.0E8,%n" +
                        "      \"duration_units\" : \"milliseconds\",%n" +
                        "      \"rate_units\" : \"calls/minute\"%n" +
                        "    }%n" +
                        "  }%n" +
                        "}"));
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo("application/json");
    }
}
