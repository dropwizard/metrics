package com.codahale.metrics.akka;

import com.codahale.metrics.*;

import java.util.Date;
import java.util.Map;

/**
 * Holder for all the collected + instrumented metrics at a particular point in time ("tick").
 */
public class MetricUpdate {
    private Map<String, Gauge> gauages;
    private Map<String, Counter> counters;
    private Map<String, Histogram> histograms;
    private Map<String, Meter> meters;
    private Map<String, Timer> timers;
    private Date date;

    /**
     * Instantiates a {@link MetricUpdate} instance.
     *
     * @param gauages       Map of {@link Gauge Gauges}
     * @param counters      Map of {@link Counter Counters}
     * @param histograms    Map of {@link Histogram Histograms}
     * @param meters        Map of {@link Meter Meters}
     * @param timers        Map of {@link Timer Timers}
     * @param date          Date/time these metrics maps were collected at
     */
    public MetricUpdate(Map<String, Gauge> gauages, Map<String, Counter> counters, Map<String, Histogram> histograms,
                        Map<String, Meter> meters, Map<String, Timer> timers, Date date) {
        super();

        setGauages(gauages);
        setCounters(counters);
        setHistograms(histograms);
        setMeters(meters);
        setTimers(timers);
        setDate(date);
    }

    public Map<String, Gauge> getGauages() {
        return gauages;
    }

    public void setGauages(Map<String, Gauge> gauages) {
        this.gauages = gauages;
    }

    public Map<String, Counter> getCounters() {
        return counters;
    }

    public void setCounters(Map<String, Counter> counters) {
        this.counters = counters;
    }

    public Map<String, Histogram> getHistograms() {
        return histograms;
    }

    public void setHistograms(Map<String, Histogram> histograms) {
        this.histograms = histograms;
    }

    public Map<String, Meter> getMeters() {
        return meters;
    }

    public void setMeters(Map<String, Meter> meters) {
        this.meters = meters;
    }

    public Map<String, Timer> getTimers() {
        return timers;
    }

    public void setTimers(Map<String, Timer> timers) {
        this.timers = timers;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
