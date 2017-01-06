package com.codahale.metrics;

public enum MetricTypes {
    MAX,
    MEAN,
    MIN,
    STDDEV,
    P50,
    P75,
    P95,
    P98,
    P99,
    P999,
    COUNT,
    M1_RATE,
    M5_RATE,
    M15_RATE,
    MEAN_RATE;

    public String toName(){
        return toString().toLowerCase();
    }
}