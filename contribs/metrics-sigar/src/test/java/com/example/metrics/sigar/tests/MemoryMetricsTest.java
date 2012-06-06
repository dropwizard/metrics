package com.example.metrics.sigar.tests;

import com.example.metrics.sigar.MemoryMetrics;
import com.example.metrics.sigar.SigarMetrics;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import static org.junit.Assert.assertThat;

public class MemoryMetricsTest {
    private final MemoryMetrics mm = SigarMetrics.getInstance().memory();

    @Test
    public void totalMemoryIsGreaterThanZero() throws Exception {
        assertThat(mm.mem().total(), is(greaterThan(0L)));
    }
    
    @Test
    public void usedMemoryIsLessThanOrEqualToTotalMemory() throws Exception {
        assertThat(mm.mem().used(), is(lessThanOrEqualTo(mm.mem().total())));
    }

    @Test
    public void freeMemoryIsLessThanTotalMemory() throws Exception {
        assertThat(mm.mem().free(), is(lessThan(mm.mem().total())));
    }

    @Test
    public void totalSwapIsGreaterThanZero() throws Exception {
        assertThat(mm.swap().total(), is(greaterThan(0L)));
    }
    
    @Test
    public void usedSwapIsLessThanOrEqualToTotalSwap() throws Exception {
        assertThat(mm.swap().used(), is(lessThanOrEqualTo(mm.swap().total())));
    }

    @Test
    public void freeSwapIsLessThanOrEqualToTotalSwap() throws Exception {
        assertThat(mm.swap().free(), is(lessThanOrEqualTo(mm.swap().total())));
    }

    @Test
    public void ramSizeIsGreaterThanZero() throws Exception {
        assertThat(mm.ramInMB(), is(greaterThan(0L)));
    }
}
