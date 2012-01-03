package com.yammer.metrics.guice.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.core.*;
import com.yammer.metrics.guice.InstrumentationModule;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class MeteredTest {

    InstrumentedWithMetered instance;
    MetricsRegistry registry;

    @Before
    public void setup() {
        final Injector injector = Guice.createInjector(new InstrumentationModule());
        instance = injector.getInstance(InstrumentedWithMetered.class);
        registry = injector.getInstance(MetricsRegistry.class);
    }

    @Test
    public void aMeteredAnnotatedMethod() throws Exception {

        instance.doAThing();

        final Metric metric = registry.allMetrics()
                                      .get(new MetricName(InstrumentedWithMetered.class,
                                                          "things"));

        assertMetricIsSetup(metric);

        assertThat("Guice creates a meter which gets marked",
                   ((Meter) metric).count(),
                   is(1L));

        assertThat("Guice creates a meter with the given event type",
                   ((Meter) metric).eventType(),
                   is("poops"));

        assertThat("Guice creates a meter with the given rate unit",
                   ((Meter) metric).rateUnit(),
                   is(TimeUnit.MINUTES));
    }

    @Test
    public void aMeteredAnnotatedMethodWithDefaultScope() throws Exception {

        final Metric metric = registry.allMetrics()
                                      .get(new MetricName(InstrumentedWithMetered.class,
                                                          "doAThingWithDefaultScope"));
        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((Meter) metric).count(),
                   is(0L));

        instance.doAThingWithDefaultScope();

        assertThat("Metric is marked",
                   ((Meter) metric).count(),
                   is(1L));
    }

    @Test
    public void aMeteredAnnotatedMethodWithProtectedScope() throws Exception {

        final Metric metric = registry.allMetrics()
                                      .get(new MetricName(InstrumentedWithMetered.class,
                                                          "doAThingWithProtectedScope"));

        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((Meter) metric).count(),
                   is(0L));

        instance.doAThingWithProtectedScope();

        assertThat("Metric is marked",
                   ((Meter) metric).count(),
                   is(1L));
    }

    private void assertMetricIsSetup(final Metric metric) {
        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a meter",
                   metric,
                   is(instanceOf(Meter.class)));
    }
}
