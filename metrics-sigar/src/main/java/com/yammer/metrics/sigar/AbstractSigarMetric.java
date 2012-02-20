package com.yammer.metrics.sigar;

import org.hyperic.sigar.Sigar;

abstract class AbstractSigarMetric {
    protected final Sigar sigar;

    protected AbstractSigarMetric(Sigar sigar) {
        this.sigar = sigar;
    }

    protected abstract void registerGauges();
}
