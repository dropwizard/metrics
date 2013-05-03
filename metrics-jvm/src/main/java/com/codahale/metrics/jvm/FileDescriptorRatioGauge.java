package com.codahale.metrics.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.codahale.metrics.NoCopyMetric;
import com.codahale.metrics.RatioGauge;

/**
 * A gauge for the ratio of used to total file descriptors.
 */
public class FileDescriptorRatioGauge extends RatioGauge implements NoCopyMetric {
    /**
   * 
   */
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
     * @param os    an {@link OperatingSystemMXBean}
     */
    public FileDescriptorRatioGauge(OperatingSystemMXBean os) {
        this.os = os;
    }

    @Override
    protected Ratio getRatio() {
        try {
            return Ratio.of(invoke("getOpenFileDescriptorCount"),
                            invoke("getMaxFileDescriptorCount"));
        } catch (NoSuchMethodException e) {
            return Ratio.of(Double.NaN, Double.NaN);
        } catch (IllegalAccessException e) {
            return Ratio.of(Double.NaN, Double.NaN);
        } catch (InvocationTargetException e) {
            return Ratio.of(Double.NaN, Double.NaN);
        }
    }

    private long invoke(String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Method method = os.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        return (Long) method.invoke(os);
    }
}
