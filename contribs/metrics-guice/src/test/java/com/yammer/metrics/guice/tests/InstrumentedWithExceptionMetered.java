package com.yammer.metrics.guice.tests;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import java.util.concurrent.TimeUnit;

public class InstrumentedWithExceptionMetered {

    @ExceptionMetered(name = "exceptionCounter")
    String explodeWithPublicScope(boolean explode) {
        if (explode) {
            throw new RuntimeException("Boom!");
        } else {
            return "calm";
        }
    }

    @ExceptionMetered
    String explodeForUnnamedMetric() {
        throw new RuntimeException("Boom!");
    }
    
    @ExceptionMetered(group="g", type="t", name="n")
    String explodeForMetricWithGroupTypeAndName() {
        throw new RuntimeException("Boom!");
    }

    @ExceptionMetered
    String explodeWithDefaultScope() {
        throw new RuntimeException("Boom!");
    }

    @ExceptionMetered
    protected String explodeWithProtectedScope() {
        throw new RuntimeException("Boom!");
    }

    @ExceptionMetered(name = "failures", cause = MyException.class)
    public void errorProneMethod(RuntimeException e) {
        throw e;
    }

    @ExceptionMetered(name = "things",
                      eventType = "poops",
                      rateUnit = TimeUnit.MINUTES,
                      cause = ArrayIndexOutOfBoundsException.class)
    public Object causeAnOutOfBoundsException() {
        final Object[] arr = {};
        return arr[1];
    }

    @Timed
    @ExceptionMetered
    public void timedAndException(RuntimeException e) {
        if (e != null) {
            throw e;
        }
    }

    @Metered
    @ExceptionMetered
    public void meteredAndException(RuntimeException e) {
        if (e != null) {
            throw e;
        }
    }
}
