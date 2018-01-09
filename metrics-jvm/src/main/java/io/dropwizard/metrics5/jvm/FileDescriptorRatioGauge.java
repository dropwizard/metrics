package io.dropwizard.metrics5.jvm;

import com.sun.management.UnixOperatingSystemMXBean;
import io.dropwizard.metrics5.RatioGauge;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * A gauge for the ratio of used to total file descriptors.
 */
public class FileDescriptorRatioGauge extends RatioGauge {
    private final OperatingSystemMXBean os;

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
        if (os instanceof UnixOperatingSystemMXBean) {
            final UnixOperatingSystemMXBean unixOs = (UnixOperatingSystemMXBean) os;
            return Ratio.of(unixOs.getOpenFileDescriptorCount(), unixOs.getMaxFileDescriptorCount());
        } else {
            return Ratio.of(Double.NaN, Double.NaN);
        }
    }
}
