package com.yammer.metrics.guice.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.core.*;
import com.yammer.metrics.guice.InstrumentationModule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TimedTest {
    @Test
    public void aTimedAnnotatedMethod() throws Exception {
        final Injector injector = Guice.createInjector(new InstrumentationModule());
        final InstrumentedWithTimed instance = injector.getInstance(InstrumentedWithTimed.class);
        final MetricsRegistry registry = injector.getInstance(MetricsRegistry.class);

        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithTimed.class,
                                                                       "things"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a timer",
                   metric,
                   is(instanceOf(TimerMetric.class)));

        assertThat("Guice creates a timer which records invocation length",
                   ((TimerMetric) metric).count(),
                   is(1L));

        assertThat("Guice creates a timer with the given rate unit",
                   ((TimerMetric) metric).rateUnit(),
                   is(TimeUnit.MINUTES));

        assertThat("Guice creates a timer with the given duration unit",
                   ((TimerMetric) metric).durationUnit(),
                   is(TimeUnit.MICROSECONDS));
    }
}
