package com.codahale.metrics;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/*
 * A tag interface to indicate that a class is a Reporter.
 */
public interface Reporter extends Closeable {

    void start(long period, TimeUnit unit);

    void start(long initialDelay, long period, TimeUnit unit);

    void stop();

    void report();

}
