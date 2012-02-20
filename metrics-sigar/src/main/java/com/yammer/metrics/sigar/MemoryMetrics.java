package com.yammer.metrics.sigar;

import java.util.List;
import java.util.ArrayList;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Swap;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class MemoryMetrics extends AbstractSigarMetric {

    protected MemoryMetrics(Sigar sigar) {
        super(sigar);
    }

    public static abstract class MemSegment {
        protected final long total;
        protected final long used;
        protected final long free;
        
        private MemSegment(long total, long used, long free) {
            this.total = total;
            this.used = used;
            this.free = free;
        }
        public long total() { return total; }
        public long used() { return used; }
        public long free() { return free; }
    }

    public static final class MainMemory extends MemSegment {
        private final long actualUsed, actualFree;
    
        private MainMemory(//
                long total, long used, long free, //
                long actualUsed, long actualFree) {
            super(total, used, free);
            this.actualUsed = actualUsed;
            this.actualFree = actualFree;
        }

        public static MainMemory fromSigarBean(Mem mem) {
            return new MainMemory( //
                    mem.getTotal(), mem.getUsed(), mem.getFree(), //
                    mem.getActualUsed(), mem.getActualFree()); 
        }

        private static MainMemory undef() {
            return new MainMemory(-1L, -1L, -1L, -1L, -1L);
        }
        
        public long actualUsed() { return actualUsed; }
        public long actualFree() { return actualFree; }
    }

    public static final class SwapSpace extends MemSegment {
        private final long pagesIn, pagesOut;

        private SwapSpace( //
                long total, long used, long free, //
                long pagesIn, long pagesOut) {
            super(total, used, free);
            this.pagesIn = pagesIn;
            this.pagesOut = pagesOut;
        }

        public static SwapSpace fromSigarBean(Swap swap) {
            return new SwapSpace( //
                    swap.getTotal(), swap.getUsed(), swap.getFree(), //
                    swap.getPageIn(), swap.getPageOut()); 
        }

        private static SwapSpace undef() {
            return new SwapSpace(-1L, -1L, -1L, -1L, -1L);
        }

        public long pagesIn() { return pagesIn; }
        public long pagesOut() { return pagesOut; }
    }

    public MainMemory mem() {
        try {
            return MainMemory.fromSigarBean(sigar.getMem());
        } catch (SigarException e) {
            return MainMemory.undef();
        }
    }

    public SwapSpace swap() {
        try {
            return SwapSpace.fromSigarBean(sigar.getSwap());
        } catch (SigarException e) {
            return SwapSpace.undef();
        }
    }

    public long ramInMB() {
        try {
            return sigar.getMem().getRam();
        } catch (SigarException e) {
            return -1L;
        }
    }

    @Override
    protected void registerGauges() {
        Metrics.newGauge(getClass(), "free", new Gauge<Long>() {
            public Long value() {
                return mem().free();
            }
        });
        Metrics.newGauge(getClass(), "actual-free", new Gauge<Long>() {
            public Long value() {
                return mem().actualFree();
            }
        });
    }

}
