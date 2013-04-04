package com.yammer.metrics.jvm.tests;

import com.yammer.metrics.jvm.FileDescriptorRatioGauge;
import org.junit.Test;

import javax.management.ObjectName;
import java.lang.management.OperatingSystemMXBean;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("UnusedDeclaration")
public class FileDescriptorRatioGaugeTest {
    private final OperatingSystemMXBean os = new OperatingSystemMXBean() {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getArch() {
            return null;
        }

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public int getAvailableProcessors() {
            return 0;
        }

        @Override
        public double getSystemLoadAverage() {
            return 0;
        }

        // these duplicate methods from UnixOperatingSystem

        private long getOpenFileDescriptorCount() {
            return 10;
        }

        private long getMaxFileDescriptorCount() {
            return 100;
        }
    };
    private final FileDescriptorRatioGauge gauge = new FileDescriptorRatioGauge(os);

    @Test
    public void calculatesTheRatioOfUsedToTotalFileDescriptors() throws Exception {
        assertThat(gauge.getValue())
                .isEqualTo(0.1);
    }

    @Test
    public void autoDetectsTheOperationSystemBean() throws Exception {
        assertThat(new FileDescriptorRatioGauge().getValue())
                .isGreaterThanOrEqualTo(0.0)
                .isLessThanOrEqualTo(1.0);
    }

    @Test
    public void returnsNaNWhenTheInformationIsUnavailable() throws Exception {
        assertThat(new FileDescriptorRatioGauge(mock(OperatingSystemMXBean.class)).getValue())
                .isNaN();
    }
}
