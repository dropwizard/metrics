package com.codahale.metrics;

import java.util.SortedMap;

public interface DeltaMetricListener {

    public abstract void report(SortedMap<String, Gauge> gauges,
                                SortedMap<String, Counter> counters,
                                SortedMap<String, Long> deltaCounters,
                                SortedMap<String, Histogram> histograms,
                                SortedMap<String, Meter> meters,
                                SortedMap<String, Timer> timers);

}
