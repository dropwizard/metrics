package com.codahale.metrics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
public class Slf4jReporterTest {

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private Logger logger = mock(Logger.class);
    private Marker marker = mock(Marker.class);

    @Before
    public void setUp() throws Exception {
        when(logger.isInfoEnabled(marker)).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdownNow();
    }

    @Test
    public void testReport() throws Exception {
        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.counter("test-counter").inc(100);

        Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(metricRegistry)
                .shutdownExecutorOnStop(false)
                .scheduleOn(executor)
                .outputTo(logger)
                .markWith(marker)
                .prefixedWith("us-nw")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
                .build();

        slf4jReporter.report();

        verify(logger).info(marker, "type=COUNTER, name=us-nw.test-counter, count=100");
    }
}
