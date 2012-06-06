package com.example.metrics.sigar.tests;

import com.example.metrics.sigar.UlimitMetrics;
import com.example.metrics.sigar.SigarMetrics;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

public class UlimitMetricsTest {
    private final UlimitMetrics um = SigarMetrics.getInstance().ulimit();

    @Test
    public void openFilesLimitIsGreaterThanZero() throws Exception {
        assertThat(um.ulimit().openFiles(), is(greaterThan(0L)));
    }
}
