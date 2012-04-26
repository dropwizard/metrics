package com.yammer.metrics.sigar.tests;

import com.yammer.metrics.sigar.UlimitMetrics;
import com.yammer.metrics.sigar.UlimitMetrics.Ulimit;
import com.yammer.metrics.sigar.SigarMetrics;

import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import static org.junit.Assert.assertThat;

public class UlimitMetricsTest {
    private final UlimitMetrics um = SigarMetrics.getInstance().ulimit();

    @Test
    public void openFilesLimitIsGreaterThanZero() throws Exception {
        assertThat(um.ulimit().openFiles(), is(greaterThan(0L)));
    }
}
