package com.codahale.metrics.jvm;

import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

public class CachedThreadStatesGaugeSetTest {

    /**
     * Test method to verify that the thread state gauges obtained from the CachedThreadStatesGaugeSet
     * are not null.
     */
    @Test
    public void testThreadStatesGaugesAreNotNull() {
        // Set cache interval to 1 second for testing
        long interval = 1;
        TimeUnit unit = TimeUnit.SECONDS;

        // Create a new CachedThreadStatesGaugeSet instance
        CachedThreadStatesGaugeSet gaugeSet = new CachedThreadStatesGaugeSet(interval, unit);

        // Retrieve the thread states gauges
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        assertNotNull(threadInfos);

        // Ensure that the gauges are not null
        assertNotNull(gaugeSet.getThreadInfo());
    }

}
