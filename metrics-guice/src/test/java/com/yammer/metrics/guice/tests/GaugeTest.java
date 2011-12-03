package com.yammer.metrics.guice.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.guice.InstrumentationModule;

import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class GaugeTest {

    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethod() throws Exception {
        final Injector injector = Guice.createInjector(new InstrumentationModule());
        final InstrumentedWithGauge instance = injector.getInstance(InstrumentedWithGauge.class);
        final MetricsRegistry registry = injector.getInstance(MetricsRegistry.class);

        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithGauge.class,
                                                                       "things"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a gauge",
                   metric,
                   is(instanceOf(GaugeMetric.class)));

        assertThat("Guice creates a gauge with the given value",
                   ((GaugeMetric<String>) metric).value(),
                   is("poop"));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void aGaugeAnnotatedMethodWithDefaultName() throws Exception {
        final Injector injector = Guice.createInjector(new InstrumentationModule());
        final InstrumentedWithGauge instance = injector.getInstance(InstrumentedWithGauge.class);
        final MetricsRegistry registry = injector.getInstance(MetricsRegistry.class);

        instance.doAnotherThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithGauge.class,
                                                                       "doAnotherThing"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a gauge",
                   metric,
                   is(instanceOf(GaugeMetric.class)));

        assertThat("Guice creates a gauge with the given value",
                   ((GaugeMetric<String>) metric).value(),
                   is("anotherThing"));
    }
}
