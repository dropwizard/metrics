package com.yammer.metrics.guice.tests;

import org.junit.Test;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.guice.InstrumentationModule;
import com.yammer.metrics.reporting.JmxReporter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DefaultJmxReporterTest
{
    public static class Tester {
        @Timed
        public void doSomething() {
        }
    }

    @Test
    public void testIt() {
        Injector injector = Guice.createInjector(new InstrumentationModule(),
                                                 new AbstractModule() {
            @Override
            protected void configure() {
                bind(Tester.class).asEagerSingleton();
            }
        });
        assertThat(injector.getInstance(JmxReporter.class),
                   is(JmxReporter.getDefault()));
    }
}
