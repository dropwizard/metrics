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

public class MeteredTest {
    @Test
    public void aMeteredAnnotatedMethod() throws Exception {
        final Injector injector = Guice.createInjector(new InstrumentationModule());
        final InstrumentedWithMetered instance = injector.getInstance(InstrumentedWithMetered.class);
        final MetricsRegistry registry = injector.getInstance(MetricsRegistry.class);

        instance.doAThing();

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithMetered.class,
                                                                       "things"));

        assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a meter",
                   metric,
                   is(instanceOf(MeterMetric.class)));

        assertThat("Guice creates a meter which gets marked",
                   ((MeterMetric) metric).count(),
                   is(1L));

        assertThat("Guice creates a meter with the given event type",
                   ((MeterMetric) metric).eventType(),
                   is("poops"));

        assertThat("Guice creates a meter with the given rate unit",
                   ((MeterMetric) metric).rateUnit(),
                   is(TimeUnit.MINUTES));

    }
}
