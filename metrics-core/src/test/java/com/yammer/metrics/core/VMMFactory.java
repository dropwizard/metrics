package com.yammer.metrics.core;

import java.lang.management.*;
import java.util.List;

public class VMMFactory {
    private VMMFactory() {}

    public static VirtualMachineMetrics build(MemoryMXBean memoryMXBean,
                                              List<MemoryPoolMXBean> memoryPoolMXBeans,
                                              OperatingSystemMXBean operatingSystemMXBean,
                                              ThreadMXBean threadMXBean,
                                              List<GarbageCollectorMXBean> garbageCollectorMXBeans,
                                              RuntimeMXBean runtimeMXBean) {
        return new VirtualMachineMetrics(memoryMXBean,
                                         memoryPoolMXBeans,
                                         operatingSystemMXBean,
                                         threadMXBean,
                                         garbageCollectorMXBeans,
                                         runtimeMXBean);
    }
}
