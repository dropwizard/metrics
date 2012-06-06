package com.example.metrics.sigar;

import org.hyperic.sigar.Sigar;

abstract class AbstractSigarMetric implements CanRegisterGauges {
    protected final Sigar sigar;

    protected AbstractSigarMetric(Sigar sigar) {
        this.sigar = sigar;
    }
}
