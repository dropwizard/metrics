package com.codahale.metrics.jersey2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jersey2.resources.InstrumentedExtendingResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class InheritedMetricsJerseyTest extends JerseyTest {

    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected Application configure() {
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

        Map<String, Timer> timers = registry.getTimers();
        int timersCount = timers.size();

        assertThat(timersCount)
            .withFailMessage(
                "Expected 3 registered timers, got %d.",
                timersCount
            )
            .isEqualTo(3);

        for (String timerName : timers.keySet()) {
            Timer timer = timers.get(timerName);
            long  count = timer.getCount();

            assertThat(count)
                .withFailMessage(
                    "Failure validating timer \"%s\": got count = %d.",
                    timerName,
                    count
                )
                .isEqualTo(1);
        }
    }

}
