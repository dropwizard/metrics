package com.codahale.metrics.akka;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.util.Date;
import java.util.Map;

/**
 * Utility class representing the values of all the instrumented metrics at a particular point in time ("tick").
 */
public class MetricsUpdate {
    private Map<String,Metric> allMetrics;
    private Date date;

    /**
     * Instantiates a {@link MetricsUpdate} instance.
     *
     * @param allMetrics A map containing all of the instrumented metrics
     * @param date  The exact point in time ("<i>tick</i>") that the metrics' values were collected at
     */
    public MetricsUpdate(Map<String, Metric> allMetrics, Date date) {
        super();

        setAllMetrics(allMetrics);
        setDate(date);
    }

    public Map<String, Metric> getAllMetrics() {
        return allMetrics;
    }

    public void setAllMetrics(Map<String, Metric> allMetrics) {
        this.allMetrics = allMetrics;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
