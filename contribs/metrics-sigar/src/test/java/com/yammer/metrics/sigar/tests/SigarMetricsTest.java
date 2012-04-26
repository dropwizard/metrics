package com.yammer.metrics.sigar.tests;

import com.yammer.metrics.sigar.SigarMetrics;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

public class SigarMetricsTest {

    @Test
    public void pidIsGreaterThanZero() throws Exception {
        assertThat(SigarMetrics.getInstance().pid(), is(greaterThan(0L)));
    }
}
