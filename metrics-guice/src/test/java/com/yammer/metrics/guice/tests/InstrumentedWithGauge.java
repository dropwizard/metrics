package com.yammer.metrics.guice.tests;

import com.yammer.metrics.guice.Gauge;

public class InstrumentedWithGauge {
    @Gauge(name = "things")
    public String doAThing() {
        return "poop";
    }

    @Gauge
    public String doAnotherThing() {
        return "anotherThing";
    }
}
