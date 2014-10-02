package com.codahale.metrics.jersey;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jersey.resources.InstrumentedResource;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

/**
 * Tests importing {@link InstrumentedResourceMethodDispatchAdapter} as a singleton
 * in a Jersey {@link com.sun.jersey.api.core.ResourceConfig}
 */
public class SingletonMetricsJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected AppDescriptor configure() {
        this.registry = new MetricRegistry();

        final DefaultResourceConfig config = new DefaultResourceConfig();
        config.getSingletons().add(new InstrumentedResourceMethodDispatchAdapter(registry));
        config.getClasses().add(InstrumentedResource.class);

        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void timedMethodsAreTimed() {
        assertThat(resource().path("timed").get(String.class))
                .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedResource.class, "timed"));

        assertThat(timer.getCount())
                .isEqualTo(1);
    }

    @Test
    public void meteredMethodsAreMetered() {
        assertThat(resource().path("metered").get(String.class))
                .isEqualTo("woo");

        final Meter meter = registry.meter(name(InstrumentedResource.class, "metered"));
        assertThat(meter.getCount())
                .isEqualTo(1);
    }

    @Test
    public void exceptionMeteredMethodsAreExceptionMetered() {
        final Meter meter = registry.meter(name(InstrumentedResource.class,
                                                "exceptionMetered",
                                                "exceptions"));

        assertThat(resource().path("exception-metered").get(String.class))
                .isEqualTo("fuh");

        assertThat(meter.getCount())
                .isZero();
        
        try {
            resource().path("exception-metered").queryParam("splode", "true").get(String.class);
            failBecauseExceptionWasNotThrown(MappableContainerException.class);
        } catch (MappableContainerException e) {
            assertThat(e.getCause())
                    .isInstanceOf(IOException.class);
        }

        assertThat(meter.getCount())
                .isEqualTo(1);

            ClientResponse response = resource().path("failure-response-metered")
                    .queryParam("splode", "true").get(ClientResponse.class);
            assertThat(response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL);

        assertThat(meter.getCount()).isEqualTo(2);
    }
}
