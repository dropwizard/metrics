package com.codahale.metrics.jersey2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.resources.InstrumentedAbstractResource;
import com.codahale.metrics.jersey2.resources.InstrumentedExtendingResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;

public class InheritedMetricsJerseyTest extends JerseyTest {

    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected Application configure() {
        MetricsResourceMethodProvider // Manual initialization
            .INSTANCE
            .initialize(false); // Search for annotations on definition methods (abstracts).

        this.registry = new MetricRegistry();

        ResourceConfig config = new ResourceConfig();

        config = config.register(new MetricsFeature(this.registry));
        config = config.register(InstrumentedExtendingResource.class);

        return config;
    }

    @Test
    public void timedMethodsFromInterfaceAreTimed() {
        assertThat(target("concrete/interface").request().get(String.class)).isEqualTo("abstract");
        assertThat(target("concrete/abstract").request().get(String.class)).isEqualTo("concrete");
        assertThat(target("concrete/concrete").request().get(String.class)).isEqualTo("yay");

        assertThat(registry.timer(name(InstrumentedExtendingResource.class, "fromConcreteClass")).getCount()).isEqualTo(1);
        assertThat(registry.timer(name(InstrumentedExtendingResource.class, "fromAbstractClass")).getCount()).isEqualTo(1);
        assertThat(registry.timer(name(InstrumentedAbstractResource.class, "interface")).getCount()).isEqualTo(1);
    }

}
