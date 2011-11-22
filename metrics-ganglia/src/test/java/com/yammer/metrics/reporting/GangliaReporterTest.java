package com.yammer.metrics.reporting;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.util.MetricPredicate;

public class GangliaReporterTest
{

    private MetricsRegistry registry;
    private GangliaReporter reporter;
    private GangliaMessage testMessage;
    
    @Before
    public void init() throws Exception
    {
        this.registry = new MetricsRegistry();
        
        //set up a test message that writes events to a string builder
        this.testMessage = new GangliaMessage(null, null, null)
        {
            private StringBuilder sb = new StringBuilder();

            @Override
            public GangliaMessage addInt(int value)
            {
                this.sb.append("addInt(" + value + ")\n");
                return this;
            }

            @Override
            public GangliaMessage addString(String value)
            {
                this.sb.append("addString(" + value + ")\n");
                return this;
            }

            @Override
            public void send() throws SocketException, IOException
            {
                this.sb.append("send()\n");
            }

            @Override
            public String toString()
            {
                return this.sb.toString();
            }
        };

        GangliaMessageBuilder messageBuilder = mock(GangliaMessageBuilder.class);

        when(messageBuilder.newMessage()).thenReturn(this.testMessage);

        this.reporter = new GangliaReporter(this.registry, "group-prefix", MetricPredicate.ALL, false, messageBuilder)
        {
            @Override
            String getHostLabel()
            {
                return "localhost";
            }
        };
        this.reporter.printVMMetrics = false;
    }

    @Test
    public void canRenderCounter() throws FileNotFoundException, IOException
    {
        String expected = getFromFile("counter.io");
        
        CounterMetric metric = this.registry.newCounter(GangliaReporterTest.class, "test");
        metric.inc(11);

        assertOutput(expected);
    }
    
    @Test
    public void canRendererGauge() throws FileNotFoundException, IOException
    {
        String expected = getFromFile("gauge.io");


        this.registry.newGauge(GangliaReporterTest.class, "test", new GaugeMetric<Long>()
        {

            @Override
            public Long value()
            {
                return 5l;
            }});
        

        assertOutput(expected);
    }
    
    @Test
    public void canRendererMetered() throws FileNotFoundException, IOException
    {
        String expected = getFromFile("metered.io");
        
        this.registry.newMeter(GangliaReporterTest.class, "test", "scope", "eventType", TimeUnit.SECONDS);

        assertOutput(expected);      
    }
    
    @Test
    public void canRendererTimed() throws FileNotFoundException, IOException
    {
        String expected = getFromFile("timed.io");
        
        this.registry.newTimer(GangliaReporterTest.class, "test", "scope", TimeUnit.SECONDS, TimeUnit.MILLISECONDS);

        assertOutput(expected);        
    }
    
    @Test
    public void canRenderHistogram() throws FileNotFoundException, IOException
    {
        String expected = getFromFile("histogram.io");
        
        this.registry.newHistogram(GangliaReporterTest.class, "test", "scope", true);

        assertOutput(expected);            
    }
    
    private void assertOutput(String expected)
    {
        this.reporter.run();
        assertEquals(expected, this.testMessage.toString());
    }

    @Test
    public void testSanitizeName_noBadCharacters() throws IOException
    {
        String metricName = "thisIsACleanMetricName";
        GangliaReporter gangliaReporter = new GangliaReporter("localhost", 5555);
        String cleanMetricName = gangliaReporter.sanitizeName(metricName);
        assertEquals("clean metric name was changed unexpectedly", metricName, cleanMetricName);
    }

    @Test
    public void testSanitizeName_badCharacters() throws IOException
    {
        String metricName = "thisIsAC>&!>leanMetric Name";
        String expectedMetricName = "thisIsAC____leanMetric_Name";
        GangliaReporter gangliaReporter = new GangliaReporter("localhost", 5555);
        String cleanMetricName = gangliaReporter.sanitizeName(metricName);
        assertEquals("clean metric name did not match expected value", expectedMetricName, cleanMetricName);
    }

    @Test
    public void testCompressPackageName() throws IOException
    {
        String metricName = "some.long.package.name.thisIsAC>&!>leanMetric Name";
        String expectedMetricName = "s.l.p.name.thisIsAC____leanMetric_Name";
        GangliaReporter gangliaReporter = new GangliaReporter("localhost", 5555, true);
        String cleanMetricName = gangliaReporter.sanitizeName(metricName);
        assertEquals("clean metric name did not match expected value", expectedMetricName, cleanMetricName);
    }
    
    protected String getFromFile(String fileName) throws FileNotFoundException, IOException
    {
        return IOUtils.toString(new FileInputStream(getClass().getClassLoader().getResource(fileName).getFile()));
    }
}