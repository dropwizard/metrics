package io.dropwizard.metrics.jetty12;

import io.dropwizard.metrics5.MetricRegistry;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedQueuedThreadPoolTest {
    private static final String PREFIX = "prefix";

    private MetricRegistry metricRegistry;
    private InstrumentedQueuedThreadPool iqtp;

    @BeforeEach
    public void setUp() {
        metricRegistry = new MetricRegistry();
        iqtp = new InstrumentedQueuedThreadPool(metricRegistry);
    }

    @Test
    public void customMetricsPrefix() throws Exception {
        iqtp.setPrefix(PREFIX);
        iqtp.start();

        assertThat(metricRegistry.getNames())
                .overridingErrorMessage("Custom metrics prefix doesn't match")
                .allSatisfy(name -> assertThat(name.getKey()).startsWith(PREFIX));

        iqtp.stop();
        assertThat(metricRegistry.getMetrics())
                .overridingErrorMessage("The default metrics prefix was changed")
                .isEmpty();
    }

    @Test
    public void metricsPrefixBackwardCompatible() throws Exception {
        iqtp.start();
        assertThat(metricRegistry.getNames())
                .overridingErrorMessage("The default metrics prefix was changed")
                .allSatisfy(name -> assertThat(name.getKey()).startsWith(QueuedThreadPool.class.getName()));

        iqtp.stop();
        assertThat(metricRegistry.getMetrics())
                .overridingErrorMessage("The default metrics prefix was changed")
                .isEmpty();
    }
}
