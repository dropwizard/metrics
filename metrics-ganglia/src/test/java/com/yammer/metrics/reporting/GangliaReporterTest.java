package com.yammer.metrics.reporting;

import org.junit.Test;
import java.io.IOException;
import static junit.framework.Assert.assertEquals;

public class GangliaReporterTest {

    @Test
    public void testSanitizeName_noBadCharacters() throws IOException {
        String metricName = "thisIsACleanMetricName";
        GangliaReporter gangliaReporter = new GangliaReporter("localhost", 5555);
        String cleanMetricName = gangliaReporter.sanitizeName(metricName);
        assertEquals("clean metric name was changed unexpectedly", metricName, cleanMetricName);
    }

    @Test
    public void testSanitizeName_badCharacters() throws IOException {
        String metricName = "thisIsAC>&!>leanMetric Name";
        String expectedMetricName = "thisIsAC____leanMetric_Name";
        GangliaReporter gangliaReporter = new GangliaReporter("localhost", 5555);
        String cleanMetricName = gangliaReporter.sanitizeName(metricName);
        assertEquals("clean metric name did not match expected value", expectedMetricName, cleanMetricName);
    }
}