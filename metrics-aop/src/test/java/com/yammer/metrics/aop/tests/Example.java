package com.yammer.metrics.aop.tests;

import com.yammer.metrics.aop.annotation.ExceptionMetered;
import com.yammer.metrics.aop.annotation.Metered;
import com.yammer.metrics.aop.annotation.Timed;

import java.io.IOException;

class Example {
    @Metered
    public String meteredMethod() {
        return "metered";
    }

    @Timed
    public String timedMethod() throws InterruptedException {
        Thread.sleep(50);
        return "timed";
    }

    @ExceptionMetered(cause = IOException.class)
    public String exceptionMethod(boolean boom) throws IOException {
        if (boom) {
            throw new IOException();
        }
        return "exception";
    }
}
