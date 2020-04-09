package com.codahale.metrics.jvm;

import com.codahale.metrics.RatioGauge;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * A gauge for the ratio of used to total file descriptors.
 */
public class FileDescriptorRatioGauge extends RatioGauge {
    private static final boolean unixOperatingSystemMXBeanExists;

    private final OperatingSystemMXBean os;

    static {
        boolean exists = false;
        try {
            Class.forName("com.sun.management.UnixOperatingSystemMXBean");
            exists = true;
        } catch (ClassNotFoundException e) {
            // do nothing
        }
        unixOperatingSystemMXBeanExists = exists;
    }

    /**
     * Creates a new gauge using the platform OS bean.
     */
    public FileDescriptorRatioGauge() {
        this(ManagementFactory.getOperatingSystemMXBean());
    }

    /**
     * Creates a new gauge using the given OS bean.
     *
     * @param os an {@link OperatingSystemMXBean}
     */
    public FileDescriptorRatioGauge(OperatingSystemMXBean os) {
        this.os = os;
    }

    @Override
    protected Ratio getRatio() {
        if (unixOperatingSystemMXBeanExists && os instanceof com.sun.management.UnixOperatingSystemMXBean) {
            final com.sun.management.UnixOperatingSystemMXBean unixOs = (com.sun.management.UnixOperatingSystemMXBean) os;
            return Ratio.of(unixOs.getOpenFileDescriptorCount(), unixOs.getMaxFileDescriptorCount());
        } else {
            return Ratio.of(Double.NaN, Double.NaN);
        }
    }
}
