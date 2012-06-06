package com.example.metrics.sigar;

import com.yammer.metrics.core.MetricsRegistry;

interface CanRegisterGauges {

    /**
     * Register zero or more Gauges in the given registry.
     */
    public void registerGauges(MetricsRegistry registry);

}


