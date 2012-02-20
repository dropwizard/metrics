package com.yammer.metrics.sigar;

import com.yammer.metrics.Metrics;
import org.hyperic.sigar.Sigar;

public class SigarMetrics {
    private static final SigarMetrics instance = new SigarMetrics();

    public static SigarMetrics getInstance() {
        return instance;
    }

    private final Sigar sigar = new Sigar();
    private final CpuMetrics cpu = new CpuMetrics(sigar); 
    private final MemoryMetrics memory = new MemoryMetrics(sigar); 

    private SigarMetrics() {
        // singleton
    }

    public void registerGauges() {
        cpu.registerGauges();
        memory.registerGauges();
    }

    public CpuMetrics cpu() {
        return cpu;
    }

    public MemoryMetrics memory() {
        return memory;
    }
}
