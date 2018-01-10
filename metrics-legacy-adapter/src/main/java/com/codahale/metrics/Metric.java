package com.codahale.metrics;

@Deprecated
public interface Metric {

    io.dropwizard.metrics5.Metric getDelegate();

    @SuppressWarnings("unchecked")
    static Metric of(io.dropwizard.metrics5.Metric metric) {
        if (metric instanceof io.dropwizard.metrics5.Counter) {
            return new Counter((io.dropwizard.metrics5.Counter) metric);
        } else if (metric instanceof io.dropwizard.metrics5.Histogram) {
            return new Histogram((io.dropwizard.metrics5.Histogram) metric);
        } else if (metric instanceof io.dropwizard.metrics5.Meter) {
            return new Meter((io.dropwizard.metrics5.Meter) metric);
        } else if (metric instanceof io.dropwizard.metrics5.Timer) {
            return new Timer((io.dropwizard.metrics5.Timer) metric);
        } else if (metric instanceof io.dropwizard.metrics5.Gauge) {
            return Gauge.of((io.dropwizard.metrics5.Gauge) metric);
        } else if (metric instanceof io.dropwizard.metrics5.MetricSet) {
            return MetricSet.of((io.dropwizard.metrics5.MetricSet) metric);
        } else {
            throw new IllegalArgumentException("Can't find adaptor class for metric of type: " + metric.getClass().getName());
        }
    }
}
