package com.yammer.metrics.sigar;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;

import org.hyperic.sigar.Sigar;

public class SigarMetrics implements CanRegisterGauges {
    private static final SigarMetrics instance = new SigarMetrics();

    public static SigarMetrics getInstance() {
        return instance;
    }

    private final Sigar sigar = new Sigar();
    private final CpuMetrics cpu = new CpuMetrics(sigar); 
    private final MemoryMetrics memory = new MemoryMetrics(sigar); 
    private final FilesystemMetrics fs = new FilesystemMetrics(sigar); 

    private SigarMetrics() {
        // singleton
    }

    /**
     * Register all gauges in the default registry.
     */
    public void registerGauges() {
        registerGauges(Metrics.defaultRegistry());
    }

    public void registerGauges(MetricsRegistry registry) {
        cpu.registerGauges(registry);
        memory.registerGauges(registry);
    }

    public CpuMetrics cpu() {
        return cpu;
    }

    public MemoryMetrics memory() {
        return memory;
    }

    public FilesystemMetrics filesystems() {
        return fs;
    }
}
