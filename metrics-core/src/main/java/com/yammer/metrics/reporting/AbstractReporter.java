package com.yammer.metrics.reporting;

public abstract class AbstractReporter implements Runnable {
    @Override
    public abstract void run();
}
