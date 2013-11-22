package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import org.junit.Test;

import java.lang.management.ClassLoadingMXBean;

import static org.fest.assertions.api.Assertions.assertThat;

public class ClassLoadingGaugeSetTest {

    private final ClassLoadingMXBean cl = new ClassLoadingMXBean() {
        @Override
        public long getTotalLoadedClassCount() {
            return 2;
        }

        @Override
        public int getLoadedClassCount() {
            return 3;
        }

        @Override
        public long getUnloadedClassCount() {
            return 1;
        }

        @Override
        public boolean isVerbose() {
            return false;
        }

        @Override
        public void setVerbose(boolean value) {

        }
    };

    private final ClassLoadingGaugeSet gauges = new ClassLoadingGaugeSet(cl);

    @Test
    public void loadedGauge() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("loaded");
        assertThat(gauge.getValue()).isEqualTo(2L);
    }

    @Test
    public void unLoadedGauge() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("unloaded");
        assertThat(gauge.getValue()).isEqualTo(1L);
    }

}
