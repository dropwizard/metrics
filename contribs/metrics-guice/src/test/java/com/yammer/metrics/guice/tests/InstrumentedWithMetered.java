package com.yammer.metrics.guice.tests;

import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import java.util.concurrent.TimeUnit;

public class InstrumentedWithMetered {
    @Metered(name = "things", eventType = "poops", rateUnit = TimeUnit.MINUTES)
    public String doAThing() {
        return "poop";
    }

    @Metered
    String doAThingWithDefaultScope() {
        return "defaultResult";
    }

    @Metered
    protected String doAThingWithProtectedScope() {
        return "defaultProtected";
    }

    @Metered(group="g", type="t", name="n")
    protected String doAThingWithGroupTypeAndName() {
        return "newGroupTypeAndName";
    }
}
