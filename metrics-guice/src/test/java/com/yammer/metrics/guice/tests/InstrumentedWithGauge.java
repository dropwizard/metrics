package com.yammer.metrics.guice.tests;

import com.yammer.metrics.annotation.Gauge;

public class InstrumentedWithGauge {
    @Gauge(name = "things")
    public String doAThing() {
        return "poop";
    }

    @Gauge
    public String doAnotherThing() {
        return "anotherThing";
    }

    @Gauge(group="g", type="t", name="n")
    public String doAThingWithGroupTypeAndName() {
        return "anotherThingWithGroupTypeAndName";
    }
}
