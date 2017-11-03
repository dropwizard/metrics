package com.codahale.metrics.akka;

import akka.actor.ActorRef;
import com.codahale.metrics.*;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class AkkaReporter extends ScheduledReporter {
    private ActorRef metricsReceiver;

    protected AkkaReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, name, filter, rateUnit, durationUnit);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {

    }
}
