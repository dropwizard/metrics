package com.codahale.metrics;

/**
 * Metric values of timer, histogram and meter
 */
public enum Value {
    TIMER_COUNT,
    TIMER_MAX,
    TIMER_MEAN,
    TIMER_MIN,
    TIMER_STDDEV,
    TIMER_P50,
    TIMER_P75,
    TIMER_P95,
    TIMER_P98,
    TIMER_P99,
    TIMER_P999,
    TIMER_M1_RATE,
    TIMER_M5_RATE,
    TIMER_M15_RATE,
    TIMER_MEAN_RATE,
    HISTOGRAM_COUNT,
    HISTOGRAM_MAX,
    HISTOGRAM_MEAN,
    HISTOGRAM_MIN,
    HISTOGRAM_STDDEV,
    HISTOGRAM_P50,
    HISTOGRAM_P75,
    HISTOGRAM_P95,
    HISTOGRAM_P98,
    HISTOGRAM_P99,
    HISTOGRAM_P999,
    METER_COUNT,
    METER_M1_RATE,
    METER_M5_RATE,
    METER_M15_RATE,
    METER_MEAN_RATE;
}