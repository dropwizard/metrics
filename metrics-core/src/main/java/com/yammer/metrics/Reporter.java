package com.yammer.metrics;

import java.util.SortedMap;

public interface Reporter {
    void report(SortedMap<String, Gauge> gauges,
                SortedMap<String, Counter> counters,
                SortedMap<String, Histogram> histograms,
                SortedMap<String, Meter> meters,
                SortedMap<String, Timer> timers);
}
