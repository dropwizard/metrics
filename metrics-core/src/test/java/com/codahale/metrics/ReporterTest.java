package com.codahale.metrics;

import org.junit.Test;
import org.mockito.Mockito;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ReporterTest {

    @Test
    public void ScheduledReporterIsReporter() {
        ScheduledReporter scheduledReporter = mock(ScheduledReporter.class, Mockito.CALLS_REAL_METHODS);
        assertThat(scheduledReporter).isInstanceOf(Reporter.class);
    }

    @Test
    public void JmxReporterIsReporter() {
        JmxReporter jmxReporter = mock(JmxReporter.class);
        assertThat(jmxReporter).isInstanceOf(Reporter.class);
    }
}
