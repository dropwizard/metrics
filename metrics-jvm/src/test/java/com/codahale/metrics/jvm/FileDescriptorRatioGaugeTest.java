package com.codahale.metrics.jvm;

import org.junit.Test;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
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

        // overridden on Java 1.7; random crap on Java 1.6
        @Override
        public ObjectName getObjectName() {
            return null;
        }
    };

    private final FileDescriptorRatioGauge gauge = new FileDescriptorRatioGauge(os);

    @Test
    public void calculatesTheRatioOfUsedToTotalFileDescriptors() throws Exception {
        assertThat(gauge.getValue())
                .isEqualTo(0.1);
    }

    @Test
    public void validateFileDescriptorRatioPresenceOnNixPlatforms() throws Exception {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        assumeTrue(osBean instanceof com.sun.management.UnixOperatingSystemMXBean);
        
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
