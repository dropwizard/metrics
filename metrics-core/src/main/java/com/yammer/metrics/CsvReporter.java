package com.yammer.metrics;

import java.util.SortedMap;

// TODO: 3/10/13 <coda> -- implement CsvReporter

public class CsvReporter extends AbstractPollingReporter {
    /**
     * Creates a new {@link CsvReporter} instance.
     *
     * @param registry the {@link MetricRegistry} containing the metrics this reporter will report
     */
    protected CsvReporter(MetricRegistry registry) {
        super(registry, "csv-reporter");
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
    }
}
