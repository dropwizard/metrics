package com.codahale.metrics.jersey2;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.exception.mapper.TestExceptionMapper;
import com.codahale.metrics.jersey2.resources.InstrumentedResourceResponseMeteredPerClass;
import com.codahale.metrics.jersey2.resources.InstrumentedSubResourceResponseMeteredPerClass;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests registering {@link InstrumentedResourceMethodApplicationListener} as a singleton
 * in a Jersey {@link ResourceConfig}
 */
public class SingletonMetricsResponseMeteredPerClassJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected Application configure() {
        this.registry = new MetricRegistry();


        ResourceConfig config = new ResourceConfig();

        config = config.register(new MetricsFeature(this.registry));
        config = config.register(InstrumentedResourceResponseMeteredPerClass.class);
        config = config.register(new TestExceptionMapper());

        return config;
    }

    @Test
    public void responseMetered2xxPerClassMethodsAreMetered() {
        assertThat(target("responseMetered2xxPerClass")
                .request()
                .get().getStatus())
                .isEqualTo(200);

        final Meter meter2xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
                "responseMetered2xxPerClass",
                "2xx-responses"));

        assertThat(meter2xx.getCount()).isEqualTo(1);
    }

    @Test
    public void responseMetered4xxPerClassMethodsAreMetered() {
        assertThat(target("responseMetered4xxPerClass")
                .request()
                .get().getStatus())
                .isEqualTo(400);
        assertThat(target("responseMeteredBadRequestPerClass")
                .request()
                .get().getStatus())
                .isEqualTo(400);

        final Meter meter4xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
                "responseMetered4xxPerClass",
                "4xx-responses"));
        final Meter meterException4xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
                "responseMeteredBadRequestPerClass",
                "4xx-responses"));

        assertThat(meter4xx.getCount()).isEqualTo(1);
        assertThat(meterException4xx.getCount()).isEqualTo(1);
    }

    @Test
    public void responseMetered5xxPerClassMethodsAreMetered() {
        assertThat(target("responseMetered5xxPerClass")
                .request()
                .get().getStatus())
                .isEqualTo(500);

        final Meter meter5xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
                "responseMetered5xxPerClass",
                "5xx-responses"));

        assertThat(meter5xx.getCount()).isEqualTo(1);
    }

    @Test
    public void responseMeteredMappedExceptionPerClassMethodsAreMetered() {
        assertThat(target("responseMeteredTestExceptionPerClass")
                .request()
                .get().getStatus())
                .isEqualTo(500);

        final Meter meterTestException = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
                "responseMeteredTestExceptionPerClass",
                "5xx-responses"));

        assertThat(meterTestException.getCount()).isEqualTo(1);
    }

    @Test
    public void responseMeteredUnmappedExceptionPerClassMethodsAreMetered() {
        try {
            target("responseMeteredRuntimeExceptionPerClass")
                    .request()
                    .get();
            fail("expected RuntimeException");
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
        }

        final Meter meterException5xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
                "responseMeteredRuntimeExceptionPerClass",
                "5xx-responses"));

        assertThat(meterException5xx.getCount()).isEqualTo(1);
    }

    @Test
    public void subresourcesFromLocatorsRegisterMetrics() {
        assertThat(target("subresource/responseMeteredPerClass")
                .request()
                .get().getStatus())
                .isEqualTo(200);

        final Meter meter = registry.meter(name(InstrumentedSubResourceResponseMeteredPerClass.class,
                "responseMeteredPerClass",
                "2xx-responses"));
        assertThat(meter.getCount()).isEqualTo(1);
    }
}
