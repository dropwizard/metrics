package com.yammer.metrics.guice.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.core.*;
import com.yammer.metrics.guice.InstrumentationModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TimedTest {
    private InstrumentedWithTimed instance;
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
        this.instance = injector.getInstance(InstrumentedWithTimed.class);
    }

    @After
    public void tearDown() throws Exception {
        registry.shutdown();
    }

    @Test
    public void aTimedAnnotatedMethod() throws Exception {

        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithTimed.class,
                                                                       "things"));

        assertMetricSetup(metric);

        assertThat("Guice creates a timer which records invocation length",
                   ((Timer) metric).count(),
                   is(1L));

        assertThat("Guice creates a timer with the given rate unit",
                   ((Timer) metric).rateUnit(),
                   is(TimeUnit.MINUTES));

        assertThat("Guice creates a timer with the given duration unit",
                   ((Timer) metric).durationUnit(),
                   is(TimeUnit.MICROSECONDS));
    }

    @Test
    public void aTimedAnnotatedMethodWithDefaultScope() throws Exception {

        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithTimed.class,
                                                                       "doAThingWithDefaultScope"));

        assertMetricSetup(metric);
    }

    @Test
    public void aTimedAnnotatedMethodWithProtectedScope() throws Exception {

        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithTimed.class,
                                                                       "doAThingWithProtectedScope"));

        assertMetricSetup(metric);
    }

    @Test
    public void aTimedAnnotatedMethodWithCustomGroupTypeAndName() throws Exception {

        instance.doAThingWithCustomGroupTypeAndName();

        final Metric metric = registry.allMetrics().get(new MetricName("g", "t", "n"));

        assertMetricSetup(metric);
    }

    private void assertMetricSetup(final Metric metric) {
        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a timer",
                   metric,
                   is(instanceOf(Timer.class)));
    }
}
