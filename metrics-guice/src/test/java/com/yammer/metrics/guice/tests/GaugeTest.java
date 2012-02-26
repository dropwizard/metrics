package com.yammer.metrics.guice.tests;

import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.guice.InstrumentationModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class GaugeTest {
    private InstrumentedWithGauge instance;
    private MetricsRegistry registry;

    @Before
    public void setup() {
        this.registry = new MetricsRegistry();
        final Injector injector = Guice.createInjector(new InstrumentationModule() {
            @Override
            protected MetricsRegistry createMetricsRegistry() {
                return registry;
            }
        });
        this.instance = injector.getInstance(InstrumentedWithGauge.class);
    }

    @After
    public void tearDown() throws Exception {
        registry.shutdown();
    }


    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethod() throws Exception {
        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithGauge.class,
                                                                       "things"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a gauge",
                   metric,
                   is(instanceOf(Gauge.class)));

        assertThat("Guice creates a gauge with the given value",
                   ((Gauge<String>) metric).value(),
                   is("poop"));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethodWithDefaultName() throws Exception {
        instance.doAnotherThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithGauge.class,
                                                                       "doAnotherThing"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a gauge",
                   metric,
                   is(instanceOf(Gauge.class)));

        assertThat("Guice creates a gauge with the given value",
                   ((Gauge<String>) metric).value(),
                   is("anotherThing"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethodWithGroupTypeAndName() throws Exception {
        final Injector injector = Guice.createInjector(new InstrumentationModule());
        final InstrumentedWithGauge instance = injector.getInstance(InstrumentedWithGauge.class);
        final MetricsRegistry registry = injector.getInstance(MetricsRegistry.class);

        instance.doAThingWithGroupTypeAndName();

        Set<MetricName> keySet = registry.allMetrics().keySet();
        final Metric metric = registry.allMetrics().get(new MetricName("g", "t", "n"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a gauge",
                   metric,
                   is(instanceOf(Gauge.class)));

        assertThat("Guice creates a gauge with the given value",
                   ((Gauge<String>) metric).value(),
                   is("anotherThingWithGroupTypeAndName"));
    }
}
