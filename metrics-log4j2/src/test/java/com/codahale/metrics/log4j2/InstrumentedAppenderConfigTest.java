package com.codahale.metrics.log4j2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedAppenderConfigTest {
    public static final String METRIC_NAME_PREFIX = "metrics";
    public static final String REGISTRY_NAME = "shared-metrics-registry";

    private final MetricRegistry registry = SharedMetricRegistries.getOrCreate(REGISTRY_NAME);
    private ConfigurationSource source;
    private LoggerContext context;

    @Before
    public void setUp() throws Exception {
        source = new ConfigurationSource(this.getClass().getClassLoader().getResourceAsStream("log4j2-testconfig.xml"));
        context = Configurator.initialize(null, source);
    }

    @After
    public void tearDown() throws Exception {
        context.stop();
    }

    // The biggest test is that we can initialize the log4j2 config at all.

    @Test
    public void canRecordAll() throws Exception {
        Logger logger = context.getLogger(this.getClass().getName());

        long initialAllCount = registry.meter(METRIC_NAME_PREFIX + ".all").getCount();
        logger.error("an error message");
        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(initialAllCount + 1);
    }

    @Test
    public void canRecordError() throws Exception {
        Logger logger = context.getLogger(this.getClass().getName());

        long initialErrorCount = registry.meter(METRIC_NAME_PREFIX + ".error").getCount();
        logger.error("an error message");
        assertThat(registry.meter(METRIC_NAME_PREFIX + ".all").getCount())
                .isEqualTo(initialErrorCount + 1);
    }

    @Test
    public void noInvalidRecording() throws Exception {
        Logger logger = context.getLogger(this.getClass().getName());

        long initialInfoCount = registry.meter(METRIC_NAME_PREFIX + ".info").getCount();
        logger.error("an error message");
        assertThat(registry.meter(METRIC_NAME_PREFIX + ".info").getCount())
                .isEqualTo(initialInfoCount);
    }

}
