package com.yammer.metrics.util.tests;

import com.yammer.metrics.util.JmxGauge;
import org.junit.Before;
import org.junit.Test;

import static java.lang.management.ManagementFactory.getCompilationMXBean;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JmxGaugeTest {
    private JmxGauge gauge;

    @Before
    public void setUp() throws Exception {
        this.gauge = new JmxGauge("java.lang:type=Compilation",
                                  "CompilationTimeMonitoringSupported");
    }

    @Test
    public void queriesJmxForGaugeValues() throws Exception {
        assertThat(gauge.value(),
                   is((Object) getCompilationMXBean().isCompilationTimeMonitoringSupported()));
    }
}
