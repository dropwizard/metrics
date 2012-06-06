package com.example.metrics.sigar.tests;

import com.example.metrics.sigar.CpuMetrics;
import com.example.metrics.sigar.CpuMetrics.CpuTime;
import com.example.metrics.sigar.SigarMetrics;

import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CpuMetricsTest {
    private final CpuMetrics cm = SigarMetrics.getInstance().cpu();

    @Test
    public void cpuCoreCountIsGreaterThanZero() throws Exception {
        assertThat(cm.totalCoreCount(), is(greaterThan(0)));
    }
    
    @Test
    public void coreCountIsAtLeastPhysicalCpuCount() throws Exception {
        assertThat(cm.totalCoreCount(), is(greaterThanOrEqualTo(cm.physicalCpuCount())));
    }

    @Test
    public void lengthOfCpuListMatchesCoreCount() throws Exception {
        assertThat(cm.cpus().size(), is(equalTo(cm.totalCoreCount())));
    }

    @Test
    public void cpuTimesAddUpToApproximatelyOne() throws Exception {
        CpuTime t = cm.cpus().get(0);
        assertThat(t.user() + t.sys() + t.nice() + t.waiting() + t.idle() + t.irq(), 
                is(closeTo(1.0, 0.05)));
    }
}
