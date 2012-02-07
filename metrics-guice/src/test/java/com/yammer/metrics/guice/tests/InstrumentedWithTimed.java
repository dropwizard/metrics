package com.yammer.metrics.guice.tests;

import com.yammer.metrics.annotation.Timed;

import java.util.concurrent.TimeUnit;

public class InstrumentedWithTimed {
    @Timed(name = "things", rateUnit = TimeUnit.MINUTES, durationUnit = TimeUnit.MICROSECONDS)
    public String doAThing() {
        return "poop";
    }

    @Timed
    String doAThingWithDefaultScope() {
        return "defaultResult";
    }

    @Timed
    protected String doAThingWithProtectedScope() {
        return "defaultProtected";
    }

    @Timed(group="g", type="t", name="n")
    protected String doAThingWithCustomGroupTypeAndName() {
        return "defaultProtected";
    }
}
