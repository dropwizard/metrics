package com.example.metrics.sigar;

import java.util.List;
import java.util.ArrayList;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.util.PercentGauge;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class CpuMetrics extends AbstractSigarMetric {
    private static final long HACK_DELAY_MILLIS = 1000;

    private final CpuInfo info;

    public static final class CpuTime {
        private final double user;
        private final double sys;
        private final double nice;
        private final double waiting;
        private final double idle;
        private final double irq;
    
        public CpuTime( //
                double user, double sys, //
                double nice, double waiting, //
                double idle, double irq) {
            this.user = user;
            this.sys = sys;
            this.nice = nice;
            this.waiting = waiting;
            this.idle = idle;
            this.irq = irq;
        }

        public static CpuTime fromSigarBean(CpuPerc cp) {
            return new CpuTime( //
                    cp.getUser(), cp.getSys(), //
                    cp.getNice(), cp.getWait(), //
                    cp.getIdle(), cp.getIrq());
        }

        public double user() { return user; }
        public double sys() { return sys; }
        public double nice() { return nice; }
        public double waiting() { return waiting; }
        public double idle() { return idle; }
        public double irq() { return irq; }
    }

    public void registerGauges(MetricsRegistry registry) {
        registry.newGauge(getClass(), "total-cores", new Gauge<Integer>() {
            public Integer getValue() {
                return totalCoreCount();
            }
        });
        registry.newGauge(getClass(), "physical-cpus", new Gauge<Integer>() {
            public Integer getValue() {
                return physicalCpuCount();
            }
        });
        registry.newGauge(getClass(), "cpu-time-user-percent", new PercentGauge() {
            public double getNumerator() {
                List<CpuTime> cpus = cpus();
                double userTime = 0.0;
                for (CpuTime cpu : cpus) {
                    userTime += cpu.user();
                }
                return userTime;
            }
            public double getDenominator() {
                return 1.0;
            }
        });
        registry.newGauge(getClass(), "cpu-time-sys-percent", new PercentGauge() {
            public double getNumerator() {
                List<CpuTime> cpus = cpus();
                double userTime = 0.0;
                for (CpuTime cpu : cpus) {
                    userTime += cpu.sys();
                }
                return userTime;
            }
            public double getDenominator() {
                return 1.0;
            }
        });
    }

    protected CpuMetrics(Sigar sigar) {
        super(sigar);
        info = cpuInfo();
    }

    public int totalCoreCount() {
        if (info == null) {
            return -1;
        }
        return info.getTotalCores(); 
    }

    public int physicalCpuCount() {
        if (info == null) {
            return -1;
        }
       return info.getTotalSockets();
    }

    public List<CpuTime> cpus() {
        List<CpuTime> result = new ArrayList<CpuTime>();
        CpuPerc[] cpus = cpuPercList();
        if (cpus == null) {
            return result;
        }

        if (Double.isNaN(cpus[0].getIdle())) {
            /*
             * XXX: Hacky workaround for strange Sigar behaviour.
             * If you call sigar.getCpuPerfList() too often(?), 
             * it returns a steaming pile of NaNs. 
             *
             * See suspicious code here:
             * https://github.com/hyperic/sigar/blob/master/bindings/java/src/org/hyperic/sigar/Sigar.java#L345-348
             *
             */ 
            try {
                Thread.sleep(HACK_DELAY_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return result;
            }
            cpus = cpuPercList();
            if (cpus == null) {
                return result;
            }
        }
        for (CpuPerc cp : cpus) {
            result.add(CpuTime.fromSigarBean(cp)); 
        }
        return result;
   }

    private CpuInfo cpuInfo() {
        try {
            CpuInfo[] infos = sigar.getCpuInfoList();
            if (infos == null || infos.length == 0) {
                return null;
            }
            return infos[0];
        } catch (SigarException e) {
            // give up
            return null;
        }
    }

    private CpuPerc[] cpuPercList() {
        CpuPerc[] cpus = null;
        try {
            cpus = sigar.getCpuPercList();
        } catch (SigarException e) {
            // give up
        }
        if (cpus == null || cpus.length == 0) {
            return null;
        }
        return cpus;
    }

}
